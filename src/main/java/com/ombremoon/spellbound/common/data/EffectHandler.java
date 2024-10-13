package com.ombremoon.spellbound.common.data;

import com.ombremoon.spellbound.common.init.AttributesInit;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.tslat.smartbrainlib.APIOnly;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public class EffectHandler implements INBTSerializable<CompoundTag> {
    private final Map<Effect, Float> buildUp = new HashMap<>();
    private LivingEntity self;

    /**
     * Checks if the entity is rooted (cant move)
     * @param entity The entity to check if rooted
     * @return true if they are rooted (cant move), false otherwise
     */
    public static boolean isRooted(LivingEntity entity) {
        return entity.hasEffect(EffectInit.ROOTED)
                || isStunned(entity)
                || SpellUtil.getSpellHandler(entity).isStationary();
    }

    /**
     * Checks if the entity is silenced (cant cast spells)
     * @param entity The entity to check if its silenced
     * @return true if silenced (cant cast spells), false otherwise
     */
    public static boolean isSilenced(LivingEntity entity) {
        return entity.hasEffect(EffectInit.SILENCED)
                || isStunned(entity);
    }

    /**
     * Checks if the entity is stunned (cant move/cast spells/use items/break blocks/turn)
     * @param entity The entity to check if its stunned
     * @return true if stunned, false otherwise
     */
    public static boolean isStunned(LivingEntity entity) {
        return entity.hasEffect(EffectInit.STUNNED)
                || entity.hasEffect(EffectInit.CATALEPSY);
    }

    /**
     * Initialises the effect handler
     * @param self the entity to attach the effect handler to
     */
    public void init(LivingEntity self) {
        this.self = self;
    }

    /**
     * Checks if the effect handler has been initialised
     * @return true if initialised, false otherwise
     */
    public boolean isInitialised() { return self != null; }

    /**
     * Increments a given status effect by a set amount, will apply the effect upon reaching 100
     * @param effect The status effect to add buildup to
     * @param amount amount to increase the build up by
     */
    public void increment(Effect effect, int amount) {
        Float progress = buildUp.get(effect);
        float postResistAmount = amount * (1f - effect.getEntityResistance(self)/100f);
        if (progress == null) progress = postResistAmount;
        else progress = Math.clamp(progress + postResistAmount, 0, 100);

        buildUp.put(effect, progress);
        if (progress >= 100) tryApplyEffect(effect);
    }

    /**
     * Gets the current build up for a given status effect
     * @param effect the effect to check the build up for
     * @return current buildup (0-100)
     */
    public float getBuildUp(Effect effect) {
        return buildUp.get(effect);
    }

    /**
     * Clears all of an effects buildup and removes the effect if present.
     * @param effect the effect to clear
     */
    public void clearEffect(Effect effect) {
        buildUp.put(effect, 0f);
        self.removeEffect(effect.getEffect());
    }

    /**
     * Clears all build ups progress and removes all status effects
     */
    public void clearAll() {
        buildUp.keySet().forEach(this::clearEffect);
    }

    /**
     * Ticks the build up effects, decreasing the progress by 1 per second
     * @param tickCount the tickcount of the entity currently ticking
     */
    @ApiStatus.Internal
    public void tick(int tickCount) {
        if (self == null) throw new RuntimeException("Status Effects not initialised.");
        if (tickCount % 20 == 0) buildUp.replaceAll((effect, amount) -> amount > 0 ? amount-1 : 0);
    }

    /**
     * Attempts to add the mob effect tied to an effect to the player
     * @param effect the effect that has reached 100 buildup
     */
    private void tryApplyEffect(Effect effect) {
        if (effect.getEffect() == null) return;
        self.addEffect(new MobEffectInstance(effect.getEffect(), 60));
    }
    
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (Effect effect : this.buildUp.keySet()) {
            tag.putFloat(effect.name(), this.buildUp.get(effect));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        for (Effect effect : Effect.values()) {
            if (tag.contains(effect.name())) {
                this.buildUp.put(effect, tag.getFloat(effect.name()));
            }
        }
    }

    public enum Effect {
        FIRE(EffectInit.INFLAMED, AttributesInit.FIRE_SPELL_RESIST),
        FROST(EffectInit.FROZEN, AttributesInit.FROST_SPELL_RESIST),
        SHOCK(EffectInit.SHOCKED, AttributesInit.SHOCK_SPELL_RESIST),
        WIND(EffectInit.WIND, AttributesInit.WIND_SPELL_RESIST),
        EARTH(EffectInit.EARTH, AttributesInit.EARTH_SPELL_RESIST),
        POISON(EffectInit.POISON, AttributesInit.POISON_SPELL_RESIST),
        DISEASE(EffectInit.DISEASE, AttributesInit.DISEASE_SPELL_RESIST);

        private final Holder<MobEffect> mobEffect;
        private final Holder<Attribute> resistance;

        Effect(Holder<MobEffect> mobEffect, Holder<Attribute> resistance) {
            this.mobEffect = mobEffect;
            this.resistance = resistance;
        }

        /**
         * Gets the mob effect which is applied when this status reaches 100
         * @return The MobEffect applied when buildup is 100
         */
        public Holder<MobEffect> getEffect() {
            return mobEffect;
        }

        /**
         * The attribute which affects the amount of buildup applied
         * @return the attribute used in buildup calculations
         */
        public Holder<Attribute> getResistance() {
            return resistance;
        }

        /**
         * Gets the value of the resistance attribute that an entity has for negating this effect build up
         * @param entity The entity to check resistance for
         * @return The resistance the entity has to the effect (-100 - 100)
         */
        public float getEntityResistance(LivingEntity entity) {
            return (float) entity.getAttribute(resistance).getValue();
        }
    }
}
