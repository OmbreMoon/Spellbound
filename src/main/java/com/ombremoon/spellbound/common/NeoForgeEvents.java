package com.ombremoon.spellbound.common;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.events.PlayerJumpEvent;
import com.ombremoon.spellbound.networking.PayloadHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            Player player = event.getEntity();
            SpellHandler handler = player.getData(DataInit.SPELL_HANDLER);
            for (int mob : handler.getSummonsForRemoval(player.tickCount)) {
                if (level.getEntity(mob) != null) level.getEntity(mob).kill();
            }
            //TODO: FIX DUCK
            //handler.save(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player player && player.level() instanceof ServerLevel level) clearSummons(level, player);
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

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            player.getData(DataInit.SPELL_HANDLER).getListener().fireEvent(SpellEventListener.Event.JUMP, new PlayerJumpEvent(player, event));
    }
}
