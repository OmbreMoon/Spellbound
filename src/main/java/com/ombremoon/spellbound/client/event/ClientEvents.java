package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.client.KeyBinds;
import com.ombremoon.spellbound.client.gui.CastModeOverlay;
import com.ombremoon.spellbound.client.renderer.MushroomRenderer;
import com.ombremoon.spellbound.client.renderer.ShadowGateRenderer;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

public class ClientEvents {

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents {

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBinds.SWITCH_MODE_BINDING);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntityInit.MUSHROOM.get(), MushroomRenderer::new);
            event.registerEntityRenderer(EntityInit.SHADOW_GATE.get(), ShadowGateRenderer::new);
        }

        @SubscribeEvent
        public static void registerGuisOverlays(RegisterGuiLayersEvent event) {
            event.registerAboveAll(CommonClass.customLocation("selected_spell_overlay"),
                    CastModeOverlay::new);

        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Player player = Minecraft.getInstance().player;
            if (KeyBinds.SWITCH_MODE_BINDING.consumeClick()) {
                SpellHandler handler = SpellUtil.getSpellHandler(player);
                handler.switchMode();
                player.displayClientMessage(Component.literal("Switched to " + (handler.inCastMode() ? "Cast mode" : "Normal mode")), true);
                PayloadHandler.switchMode();
            }
        }

        @SubscribeEvent
        public static void onMovementInput(MovementInputUpdateEvent event) {
            if (event.getEntity().hasEffect(EffectInit.ROOTED) || event.getEntity().hasEffect(EffectInit.STUNNED)) {
                event.getInput().leftImpulse = 0;
                event.getInput().forwardImpulse = 0;
                event.getInput().jumping = false;
            }
        }

    }
}
