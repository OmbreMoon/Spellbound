package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.content.entity.ISpellEntity;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.content.entity.SmartSpellEntity;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.buff.events.ChangeTargetEvent;
import com.ombremoon.spellbound.common.magic.api.buff.events.DamageEvent;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class SummonSpell extends AnimatedSpell {
    private static final ResourceLocation DAMAGE_EVENT = CommonClass.customLocation("summon_damage_event");
    private static final ResourceLocation TARGETING_EVENT = CommonClass.customLocation("summon_targeting_event");
    private final Set<Integer> summons = new IntOpenHashSet();
    private final double spawnRange;

    @SuppressWarnings("unchecked")
    public static <T extends SummonSpell> Builder<T> createSummonBuilder(Class<T> spellClass) {
        return (Builder<T>) new Builder<>().castCondition((context, spell) -> spell.hasValidSpawnPos(spell.spawnRange));
    }

    public SummonSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
        this.spawnRange = builder.spawnRange;
    }

    /**
     * Attaches event listeners to hande summon targeting
     * @param context the context of the spells
     */
    @Override
    protected void onSpellStart(SpellContext context) {
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.POST_DAMAGE, DAMAGE_EVENT, this::damageEvent);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.CHANGE_TARGET, TARGETING_EVENT, this::changeTargetEvent);
    }

    /**
     * Discards the summons and removes the event listeners
     * @param context the context of the spells
     */
    @Override
    protected void onSpellStop(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            for (int summonId : summons) {
                Entity entity = level.getEntity(summonId);
                if (entity != null) {
                    if (entity instanceof SmartSpellEntity) {
                        //SET DESPAWN ANIMATIONS
                        log("Smart Entity");
                        entity.discard();
                    } else {
                        entity.discard();
                    }
                }
            }
        }

        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.POST_DAMAGE, DAMAGE_EVENT);
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.CHANGE_TARGET, TARGETING_EVENT);
    }

    /**
     * Returns the IDs of all summons created by this spells
     * @return Set of entity IDs
     */
    public Set<Integer> getSummons() {
        return this.summons;
    }

    @Override
    public <T extends Entity & ISpellEntity<?>> T summonEntity(SpellContext context, EntityType<T> entityType, double range, Consumer<T> extraData) {
        T entity = super.summonEntity(context, entityType, range, extraData);
        this.summons.add(entity.getId());
        return entity;
    }

    protected final void setSummonsTarget(Level level, Set<Integer> summons, LivingEntity target) {
        for (int mobId : summons) {
            if (level.getEntity(mobId) instanceof PathfinderMob mob)
                SpellUtil.setTarget(mob, target);
        }
    }

    protected final void damageEvent(DamageEvent.Post damageEvent) {
        Entity sourceEntity = damageEvent.getSource().getEntity();
        LivingEntity damageEntity = damageEvent.getEntity();

        if (sourceEntity == null) return;
        LivingEntity caster = damageEvent.getCaster();

        if (sourceEntity.is(caster)) {
            if (!damageEntity.is(caster) && !SpellUtil.isSummonOf(damageEntity, caster))
                setSummonsTarget(damageEntity.level(), getSummons(), damageEntity);
        } else if (damageEntity.is(caster) && sourceEntity instanceof LivingEntity livingEntity) {
            if (!SpellUtil.isSummonOf(livingEntity, caster))
                setSummonsTarget(livingEntity.level(), getSummons(), livingEntity);
        }
    }

    private void changeTargetEvent(ChangeTargetEvent targetEvent) {
        LivingChangeTargetEvent event = targetEvent.getTargetEvent();

        if (event.getNewAboutToBeSetTarget() == null) return;

        Entity owner = SpellUtil.getOwner(event.getEntity());
        if (owner == null) return;

        LivingEntity target = SpellUtil.getTarget(event.getEntity());
        event.setNewAboutToBeSetTarget(target);
    }

    public static class Builder<T extends SummonSpell> extends AnimatedSpell.Builder<T> {
        private double spawnRange = 5;

        public Builder<T> mastery(SpellMastery mastery) {
            this.spellMastery = mastery;
            return this;
        }

        public Builder<T> manaCost(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public Builder<T> duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder<T> baseDamage(int baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }

        public Builder<T> xpModifier(float modifier) {
            this.xpModifier = modifier;
            return this;
        }

        public Builder<T> castTime(int castTime) {
            this.castTime = castTime;
            return this;
        }

        public Builder<T> castAnimation(Function<SpellContext, String> castAnimationName) {
            this.castAnimation = castAnimationName;
            return this;
        }

        public Builder<T> failAnimation(Function<SpellContext, String> failAnimationName) {
            this.failAnimation = failAnimationName;
            return this;
        }

        public Builder<T> castCondition(BiPredicate<SpellContext, T> castCondition) {
            this.castPredicate = castCondition;
            return this;
        }

        public Builder<T> additionalCondition(BiPredicate<SpellContext, T> castCondition) {
            this.castPredicate = this.castPredicate.and(castCondition);
            return this;
        }

        public Builder<T> spawnRange(double spawnRange) {
            this.spawnRange = spawnRange;
            return this;
        }

        public Builder<T> castType(CastType castType) {
            this.castType = castType;
            return this;
        }

        public Builder<T> castSound(SoundEvent castSound) {
            this.castSound = castSound;
            return this;
        }
        public Builder<T> skipEndOnRecast(Predicate<SpellContext> skipIf) {
            this.skipEndOnRecast = skipIf;
            return this;
        }

        public Builder<T> skipEndOnRecast() {
            this.skipEndOnRecast = context -> true;
            return this;
        }

        public Builder<T> updateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public Builder<T> hasLayer() {
            this.hasLayer = true;
            return this;
        }
    }
}
