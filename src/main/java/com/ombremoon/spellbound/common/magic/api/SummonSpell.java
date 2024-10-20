package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.events.ChangeTargetEvent;
import com.ombremoon.spellbound.common.magic.events.DamageEvent;
import com.ombremoon.spellbound.util.SummonUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

public abstract class SummonSpell extends AnimatedSpell {
    private static final ResourceLocation DAMAGE_EVENT = CommonClass.customLocation("summon_damage_event");
    private static final ResourceLocation TARGETING_EVENT = CommonClass.customLocation("summon_targeting_event");

    private final Set<Integer> summons = new HashSet<>();

    @SuppressWarnings("unchecked")
    public static <T extends SummonSpell> Builder<T> createSummonBuilder(Class<T> spellClass) {
        return (Builder<T>) new Builder<>().castCondition((context, spell) -> getSpawnPos(context.getCaster(), context.getLevel()) != null);
    }

    public SummonSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
    }

    /**
     * Attaches event listeners to hande summon targeting
     * @param context the context of the spell
     */
    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);

        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.POST_DAMAGE, DAMAGE_EVENT, this::damageEvent);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.CHANGE_TARGET, TARGETING_EVENT, this::changeTargetEvent);
    }

    /**
     * Discards the summons and removes the event listeners
     * @param context the context of the spell
     */
    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);

        if (!context.getLevel().isClientSide) {
            for (int summonId : summons) {
                if (context.getLevel().getEntity(summonId) != null)
                    context.getLevel().getEntity(summonId).discard();
            }
        }

        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.POST_DAMAGE, DAMAGE_EVENT);
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.CHANGE_TARGET, TARGETING_EVENT);
    }

    /**
     * Returns the IDs of all summons created by this spell
     * @return Set of entity IDs
     */
    public Set<Integer> getSummons() {
        return this.summons;
    }

    /**
     * Spawns the desired entity as a summon a chosen number of times, where the player is looking
     * @param context the context of the spell
     * @param entityType the entity being created
     * @param mobCount number of the summon to spawn
     * @return Set containing the IDs of the summoned entities
     * @param <T> Chosen Entity
     */
    protected <T extends Entity> Set<T> summonMobs(SpellContext context, EntityType<T> entityType, int mobCount) {
        Level level = context.getLevel();
        LivingEntity caster = context.getCaster();

        Set<Integer> summonedMobs = new HashSet<>();
        Set<T> toReturn = new HashSet<>();
        BlockPos blockPos = getSpawnPos(caster, level);
        if (blockPos == null) return null;

        Vec3 spawnPos = blockPos.getCenter();
        for (int i = 0; i < mobCount; i++) {
            T summon = entityType.create(level);
            SummonUtil.setOwner(summon, caster);
            summon.teleportTo(spawnPos.x, blockPos.getY(), spawnPos.z);
            level.addFreshEntity(summon);
            summonedMobs.add(summon.getId());
            toReturn.add(summon);
        }

        this.summons.addAll(summonedMobs);
        return toReturn;
    }

    /**
     * Gets the position for the entity to spawn
     * @param caster Caster of the spell
     * @param level the Level to check blockstates on
     * @return the BlockPos of a valid spawn position, null if none found
     */
    private static BlockPos getSpawnPos(LivingEntity caster, Level level) {
        BlockHitResult blockHit = level.clip(setupRayTraceContext(caster, 5d, ClipContext.Fluid.NONE));
        if (blockHit.getType() == HitResult.Type.MISS) return null;
        if (blockHit.getDirection() == Direction.DOWN) return null;

        return blockHit.getBlockPos().relative(blockHit.getDirection());
    }

    protected final void setSummonsTarget(Level level, Set<Integer> summons, LivingEntity target) {
        for (int mobId : summons) {
            if (level.getEntity(mobId) instanceof Monster monster) {
                SummonUtil.setTarget(monster, target);
            }
        }
    }

    protected final void damageEvent(DamageEvent.Post damageEvent) {
        if (damageEvent.getSource().getEntity() == null) return;
        LivingEntity player = damageEvent.getCaster();

        if (damageEvent.getSource().getEntity().is(player)) {
            if (!damageEvent.getEntity().is(player) && !SummonUtil.isSummonOf(damageEvent.getEntity(), player))
                setSummonsTarget(damageEvent.getEntity().level(), getSummons(), damageEvent.getEntity());
        } else if (damageEvent.getEntity().is(player) && damageEvent.getSource().getEntity() instanceof LivingEntity entity) {
            if (!SummonUtil.isSummonOf(entity, player))
                setSummonsTarget(entity.level(), getSummons(), entity);
        }
    }

    private void changeTargetEvent(ChangeTargetEvent targetEvent) {
        LivingChangeTargetEvent event = targetEvent.getTargetEvent();

        if (event.getNewAboutToBeSetTarget() == null) return;

        Entity owner = SummonUtil.getOwner(event.getEntity());
        if (owner == null) return;

        LivingEntity target = SummonUtil.getTarget(event.getEntity());
        event.setNewAboutToBeSetTarget(target);
    }

    public static class Builder<T extends SummonSpell> extends AnimatedSpell.Builder<T> {
        public Builder<T> manaCost(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public Builder<T> castTime(int castTime) {
            this.castTime = castTime;
            return this;
        }

        public Builder<T> castAnimation(String castAnimationName) {
            this.castAnimation = castAnimationName;
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

        public Builder<T> duration(int duration) {
            this.duration = duration;
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

        public Builder<T> partialRecast() {
            this.partialRecast = true;
            this.fullRecast = false;
            return this;
        }

        public Builder<T> fullRecast() {
            this.fullRecast = true;
            this.partialRecast = false;
            return this;
        }

        public Builder<T> skipEndOnRecast() {
            this.skipEndOnRecast = true;
            return this;
        }

        public Builder<T> updateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }
    }
}
