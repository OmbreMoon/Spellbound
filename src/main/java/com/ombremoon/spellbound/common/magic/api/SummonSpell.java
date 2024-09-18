package com.ombremoon.spellbound.common.magic.api;

import com.mojang.logging.LogUtils;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.checkerframework.checker.units.qual.C;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SummonSpell extends AnimatedSpell {

    public SummonSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        if (context.getLevel() instanceof ServerLevel level) {
            Player player = context.getPlayer();
            SpellHandler handler = player.getData(DataInit.SPELL_HANDLER);
            for (int mob : handler.getSummonsForRemoval(this)) {
                if (level.getEntity(mob) != null) level.getEntity(mob).discard();
                LogUtils.getLogger().debug("{}", level.getEntity(mob));
            }
            handler.save(player);
        }
    }

    protected <T extends Entity> Set<Integer> addMobs(SpellContext context, EntityType<T> summon, int mobCount) {
        if (context.getLevel() instanceof ServerLevel level) {
            Player player = context.getPlayer();
            SpellHandler handler = context.getPlayer().getData(DataInit.SPELL_HANDLER);

            Set<Integer> summonedMobs = new HashSet<>();
            BlockPos blockPos = getSpawnPos(player, level);
            if (blockPos == null) return null;

            Vec3 spawnPos = blockPos.getCenter();
            for (int i = 0; i < mobCount; i++) {
                T mob = summon.create(level);
                mob.setData(DataInit.OWNER_UUID, context.getPlayer().getUUID().toString());
                mob.teleportTo(spawnPos.x, blockPos.getY(), spawnPos.z);
                level.addFreshEntity(mob);
                summonedMobs.add(mob.getId());
            }

            handler.addSummons(this, summonedMobs);
            handler.save(player);
            return summonedMobs;
        } else return null;
    }

    private BlockPos getSpawnPos(Player player, Level level) {
        BlockHitResult blockHit = level.clip(setupRayTraceContext(player, 5d, ClipContext.Fluid.NONE));
        if (blockHit.getType() == HitResult.Type.MISS) return null;
        if (blockHit.getDirection() == Direction.DOWN) return null;

        return blockHit.getBlockPos().relative(blockHit.getDirection());
    }

    @EventBusSubscriber(modid = Constants.MOD_ID)
    public static class SummonEvents {

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Post event) {
            if (event.getEntity().level().isClientSide || event.getSource().getEntity() == null) return;

            if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof LivingEntity newTarget) {
                SpellHandler handler = player.getData(DataInit.SPELL_HANDLER);
                setSummonsTarget(player.level(), handler.getAllSummons(), newTarget);
            } else if (event.getSource().getEntity() instanceof Player player
                    && !event.getEntity().getData(DataInit.OWNER_UUID).equals(player.getUUID().toString())) {
                SpellHandler handler = player.getData(DataInit.SPELL_HANDLER);
                setSummonsTarget(player.level(), handler.getAllSummons(), event.getEntity());
            }
        }

        @SubscribeEvent
        public static void onChangeTarget(LivingChangeTargetEvent event) {
            if (event.getNewAboutToBeSetTarget() == null) return;
            if (event.getEntity().getData(DataInit.OWNER_UUID).isEmpty()) return;

            int targetId = event.getEntity().getData(DataInit.TARGET_ID);

            if (targetId == 0) event.setNewAboutToBeSetTarget(null);
            else if (targetId != event.getNewAboutToBeSetTarget().getId())
                event.setNewAboutToBeSetTarget((LivingEntity) event.getEntity().level().getEntity(targetId));
        }

        @SubscribeEvent
        public static void onPlayerLeaveWorld(EntityLeaveLevelEvent event) {
            if (event.getEntity() instanceof Player player && player.level() instanceof ServerLevel level) clearSummons(level, player);
        }

        @SubscribeEvent
        public static void onWorldEnd(ServerStoppingEvent event) {
            List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
            for (ServerPlayer player : players) {
                player.getData(DataInit.SPELL_HANDLER).clearAllSummons((ServerLevel) player.level());
            }
        }

        @SubscribeEvent
        public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity().level() instanceof ServerLevel level) {
                event.getEntity().getData(DataInit.SPELL_HANDLER).getActiveSpells().clear();
                clearSummons(level, event.getEntity());
            }
        }

        private static void clearSummons(ServerLevel level, Player player) {
            SpellHandler handler = player.getData(DataInit.SPELL_HANDLER);
            handler.clearAllSummons(level);
            handler.save(player);
        }

        private static void setSummonsTarget(Level level, Set<Integer> summons, LivingEntity target) {
            for (int mobId : summons) {
                if (level.getEntity(mobId) instanceof Monster monster) {
                    monster.setData(DataInit.TARGET_ID, target.getId());
                    monster.setTarget(target);
                }
            }
        }
    }
}
