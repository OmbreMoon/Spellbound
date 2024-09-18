package com.ombremoon.spellbound.common;

import com.google.gson.annotations.Since;
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
import net.minecraft.world.entity.monster.Zombie;
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
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            player.getData(DataInit.SPELL_HANDLER).getListener().fireEvent(SpellEventListener.Event.JUMP, new PlayerJumpEvent(player, event));

    }
}
