package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.events.ChangeTargetEvent;
import com.ombremoon.spellbound.common.magic.events.PlayerDamageEvent;
import com.ombremoon.spellbound.util.SpellUtil;
import com.ombremoon.spellbound.util.SummonUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class SummonSpell extends AnimatedSpell {
    private static final UUID DAMAGE_EVENT = UUID.fromString("79e708db-b942-4ca0-a6e8-2614f087881f");
    private static final UUID TARGETING_EVENT = UUID.fromString("d1d10463-e003-4dca-a6a0-bc5300d369d6");

    private final Set<Integer> summons = new HashSet<>();

    public static Builder<AnimatedSpell> createSummonBuilder() {
        return createSimpleSpellBuilder()
                .castCondition((context, spell) -> getSpawnPos(context.getPlayer(), context.getLevel()) != null);
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
    protected <T extends Entity> Set<T> addMobs(SpellContext context, EntityType<T> entityType, int mobCount) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        Set<Integer> summonedMobs = new HashSet<>();
        Set<T> toReturn = new HashSet<>();
        BlockPos blockPos = getSpawnPos(player, level);
        if (blockPos == null) return null;

        Vec3 spawnPos = blockPos.getCenter();
        for (int i = 0; i < mobCount; i++) {
            T summon = entityType.create(level);
            SummonUtil.setOwner(summon, player);
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
     * @param player Caster of the spell
     * @param level the Level to check blockstates on
     * @return the BlockPos of a valid spawn position, null if none found
     */
    private static BlockPos getSpawnPos(Player player, Level level) {
        BlockHitResult blockHit = level.clip(setupRayTraceContext(player, 5d, ClipContext.Fluid.NONE));
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

    protected final void damageEvent(PlayerDamageEvent.Post damageEvent) {
        if (damageEvent.getSource().getEntity() == null) return;
        Player player = damageEvent.getPlayer();

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

        LivingEntity owner = SummonUtil.getOwner(event.getEntity());
        if (owner == null) return;

        LivingEntity target = SummonUtil.getTarget(event.getEntity());
        event.setNewAboutToBeSetTarget(target);
    }
}
