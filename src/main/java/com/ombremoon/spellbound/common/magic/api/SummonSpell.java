package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.content.entity.ISpellEntity;
import com.ombremoon.spellbound.common.content.entity.SmartSpellEntity;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.buff.events.ChangeTargetEvent;
import com.ombremoon.spellbound.common.magic.api.buff.events.DamageEvent;
import com.ombremoon.spellbound.common.magic.api.buff.events.PlayerAttackEvent;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class SummonSpell extends AnimatedSpell {
    private static final ResourceLocation ATTACK_EVENT = CommonClass.customLocation("summon_attack_event");
    private static final ResourceLocation DAMAGE_EVENT = CommonClass.customLocation("summon_damage_event");
    private static final ResourceLocation TARGETING_EVENT = CommonClass.customLocation("summon_targeting_event");
    private final Set<Integer> summons = new IntOpenHashSet();
    private boolean summonedEntity;

    @SuppressWarnings("unchecked")
    public static <T extends SummonSpell> Builder<T> createSummonBuilder(Class<T> spellClass) {
        return (Builder<T>) new Builder<>()
                .castCondition((context, spell) -> spell.hasValidSpawnPos());
    }

    public SummonSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
    }

    /**
     * Attaches event listeners to hande summon targeting
     * @param context the context of the spells
     */
    @Override
    protected void onSpellStart(SpellContext context) {
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.ATTACK, ATTACK_EVENT, this::attackEvent);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.POST_DAMAGE, DAMAGE_EVENT, this::damageEvent);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.CHANGE_TARGET, TARGETING_EVENT, this::changeTargetEvent);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            for (int id : this.summons) {
                Entity entity = level.getEntity(id);
                if (entity == null)
                    this.summons.remove(id);
            }

            if (this.summonedEntity && this.summons.isEmpty())
                endSpell();
        }
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

        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.ATTACK, ATTACK_EVENT);
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
    public <T extends Entity & ISpellEntity<?>> T summonEntity(SpellContext context, EntityType<T> entityType, Vec3 spawnPos, Consumer<T> extraData) {
        T entity = super.summonEntity(context, entityType, spawnPos, extraData);
        if (entity instanceof LivingEntity) {
            this.summons.add(entity.getId());
            this.summonedEntity = true;
        }

        return entity;
    }

    protected final void setSummonsTarget(Level level, LivingEntity target) {
        for (int mobId : this.summons) {
            if (level.getEntity(mobId) instanceof LivingEntity livingEntity)
                SpellUtil.setTarget(livingEntity, target);
        }
    }

    protected final void attackEvent(PlayerAttackEvent attackEvent) {
        LivingEntity entity = attackEvent.getCaster();

        if (!(entity instanceof Player)) return;

        Entity damageEntity = attackEvent.getTarget();
        Level level = damageEntity.level();

        if (!(damageEntity instanceof LivingEntity livingEntity)) return;

        if (!damageEntity.is(entity) && !SpellUtil.isSummonOf(livingEntity, entity)) {
            setSummonsTarget(level, livingEntity);
        }
    }

    protected final void damageEvent(DamageEvent.Post damageEvent) {
        Entity sourceEntity = damageEvent.getSource().getEntity();
        LivingEntity damageEntity = damageEvent.getEntity();
        Level level = damageEntity.level();

        if (sourceEntity == null) return;
        LivingEntity caster = damageEvent.getCaster();

        if (damageEntity.is(caster) && sourceEntity instanceof LivingEntity livingEntity && !SpellUtil.isSummonOf(livingEntity, caster)) {
            setSummonsTarget(level, livingEntity);
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

        public Builder() {
            this.summonCast();
        }

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

        public Builder<T> baseDamage(float baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }

        public Builder<T> xpModifier(float modifier) {
            this.xpModifier = modifier;
            return this;
        }

        public Builder<T> castTime(int castTime, int stationaryTicks) {
            this.castTime = castTime;
            this.stationaryTicks = stationaryTicks;
            return this;
        }

        public Builder<T> castTime(int castTime) {
            this.castTime = castTime;
            this.stationaryTicks = castTime;
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
