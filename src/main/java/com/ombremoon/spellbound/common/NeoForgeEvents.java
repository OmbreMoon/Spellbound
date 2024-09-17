package com.ombremoon.spellbound.common;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.events.PlayerJumpEvent;
import com.ombremoon.spellbound.networking.PayloadHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeEvents {

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player player) {
                if (!player.level().isClientSide) {
                    var handler = player.getData(DataInit.SPELL_HANDLER.get());
                    PayloadHandler.syncSpellsToClient(player);
                    handler.initData(player);
                }
            }
        }
    }

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

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            player.getData(DataInit.SPELL_HANDLER).getListener().fireEvent(SpellEventListener.Events.JUMP, new PlayerJumpEvent(player, event));

    }
}
