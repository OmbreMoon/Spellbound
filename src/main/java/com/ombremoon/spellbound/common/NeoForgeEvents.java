package com.ombremoon.spellbound.common;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.events.PlayerDamageEvent;
import com.ombremoon.spellbound.common.magic.events.PlayerJumpEvent;
import com.ombremoon.spellbound.common.magic.events.ChangeTargetEvent;
import com.ombremoon.spellbound.networking.PayloadHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeEvents {

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player player) {
                if (!player.level().isClientSide) {
                    var handler = player.getData(DataInit.SPELL_HANDLER.get());
                    var skillHandler = player.getData(DataInit.SKILL_HANDLER);
                    handler.initData(player);
                    handler.sync();
                    skillHandler.sync(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player player && player.level() instanceof ServerLevel level) {
            SpellHandler handler = player.getData(DataInit.SPELL_HANDLER);
            handler.clearAllSummons(level);
            handler.sync();
        };
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
            SpellHandler handler = event.getEntity().getData(DataInit.SPELL_HANDLER);
            handler.clearAllSummons(level);
            handler.sync();
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            player.getData(DataInit.SPELL_HANDLER).getListener().fireEvent(SpellEventListener.Events.TARGETING_EVENT, new ChangeTargetEvent(player, event));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            player.getData(DataInit.SPELL_HANDLER).getListener().fireEvent(SpellEventListener.Events.POST_DAMAGE, new PlayerDamageEvent.Post(player, event));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            player.getData(DataInit.SPELL_HANDLER).getListener().fireEvent(SpellEventListener.Events.PRE_DAMAGE, new PlayerDamageEvent.Pre(player, event));
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            player.getData(DataInit.SPELL_HANDLER).getListener().fireEvent(SpellEventListener.Events.JUMP, new PlayerJumpEvent(player, event));

    }
}
