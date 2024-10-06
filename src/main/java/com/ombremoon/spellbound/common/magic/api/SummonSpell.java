package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.AttributesInit;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.events.ChangeTargetEvent;
import com.ombremoon.spellbound.common.magic.events.PlayerDamageEvent;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.*;

public abstract class SummonSpell extends AnimatedSpell {
    private static final UUID DAMAGE_EVENT = UUID.fromString("79e708db-b942-4ca0-a6e8-2614f087881f");
    private static final UUID TARGETING_EVENT = UUID.fromString("d1d10463-e003-4dca-a6a0-bc5300d369d6");

    private final Set<Integer> summons = new HashSet<>();

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
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.TARGETING_EVENT, TARGETING_EVENT, this::changeTargetEvent);
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
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.TARGETING_EVENT, TARGETING_EVENT);
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
    protected <T extends Entity> Set<Integer> addMobs(SpellContext context, EntityType<T> entityType, int mobCount) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        Set<Integer> summonedMobs = new HashSet<>();
        BlockPos blockPos = getSpawnPos(player, level);
        if (blockPos == null) return null;

        Vec3 spawnPos = blockPos.getCenter();
        for (int i = 0; i < mobCount; i++) {
            T summon = entityType.create(level);
            summon.setData(DataInit.OWNER_UUID, context.getPlayer().getUUID().toString());
            summon.teleportTo(spawnPos.x, blockPos.getY(), spawnPos.z);
            if (summon instanceof SpellEntity entity) entity.setOwner(context.getPlayer());
            level.addFreshEntity(summon);
            summonedMobs.add(summon.getId());
        }

        this.summons.addAll(summonedMobs);
        return summonedMobs;
    }

    /**
     * Gets the position for the entity to spawn
     * @param player Caster of the spell
     * @param level the Level to check blockstates on
     * @return the BlockPos of a valid spawn position, null if none found
     */
    private BlockPos getSpawnPos(Player player, Level level) {
        BlockHitResult blockHit = level.clip(setupRayTraceContext(player, 5d, ClipContext.Fluid.NONE));
        if (blockHit.getType() == HitResult.Type.MISS) return null;
        if (blockHit.getDirection() == Direction.DOWN) return null;

        return blockHit.getBlockPos().relative(blockHit.getDirection());
    }

    /**
     * Updates all of a summons target with the current players target
     * @param level level containing the summon
     * @param summons the set of summons to update the targeting for
     * @param target the new target
     */
    private void setSummonsTarget(Level level, Set<Integer> summons, LivingEntity target) {
        for (int mobId : summons) {
            if (level.getEntity(mobId) instanceof Monster monster) {
                monster.setData(DataInit.TARGET_ID, target.getId());
                monster.setTarget(target);
            }
        }
    }

    private void damageEvent(PlayerDamageEvent.Post damageEvent) {
        LivingDamageEvent.Post event = damageEvent.getDamageEvent();

        if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof LivingEntity newTarget) {
            setSummonsTarget(player.level(), getSummons(), newTarget);
        } else if (event.getSource().getEntity() instanceof Player player
                && !event.getEntity().getData(DataInit.OWNER_UUID).equals(player.getUUID().toString())) {
            setSummonsTarget(player.level(), getSummons(), event.getEntity());
        }
    }

    private void changeTargetEvent(ChangeTargetEvent targetEvent) {
        LivingChangeTargetEvent event = targetEvent.getTargetEvent();

        if (event.getNewAboutToBeSetTarget() == null) return;
        if (event.getEntity().getData(DataInit.OWNER_UUID).isEmpty()) return;

        int targetId = event.getEntity().getData(DataInit.TARGET_ID);

        if (targetId == 0) event.setNewAboutToBeSetTarget(null);
        else if (targetId != event.getNewAboutToBeSetTarget().getId())
            event.setNewAboutToBeSetTarget((LivingEntity) event.getEntity().level().getEntity(targetId));
    }
}
