package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.client.KeyBinds;
import com.ombremoon.spellbound.client.gui.CastModeOverlay;
import com.ombremoon.spellbound.client.renderer.GenericSpellRenderer;
import com.ombremoon.spellbound.client.renderer.OutlineSpellRenderer;
import com.ombremoon.spellbound.client.renderer.ShadowGateRenderer;
import com.ombremoon.spellbound.client.renderer.entity.LivingShadowRenderer;
import com.ombremoon.spellbound.client.renderer.layer.EmissiveOutlineSpellRenderer;
import com.ombremoon.spellbound.client.renderer.layer.GenericSpellLayer;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
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
            event.registerEntityRenderer(EntityInit.MUSHROOM.get(), GenericSpellRenderer::new);
            event.registerEntityRenderer(EntityInit.SHADOW_GATE.get(), ShadowGateRenderer::new);
            event.registerEntityRenderer(EntityInit.SOLAR_RAY.get(), EmissiveOutlineSpellRenderer::new);
            event.registerEntityRenderer(EntityInit.LIVING_SHADOW.get(), LivingShadowRenderer::new);
        }

        @SubscribeEvent
        public static void registerEntityLayers(EntityRenderersEvent.AddLayers event) {
            for (final var skin : event.getSkins()) {
                final LivingEntityRenderer<Player, PlayerModel<Player>> playerRenderer = event.getSkin(skin);
                if (playerRenderer == null)
                    continue;

                playerRenderer.addLayer(new GenericSpellLayer<>(playerRenderer));
            }
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
            var handler = SpellUtil.getSpellHandler(event.getEntity());
            if (event.getEntity().hasEffect(EffectInit.ROOTED) || event.getEntity().hasEffect(EffectInit.STUNNED) || handler.isStationary()) {
                event.getInput().leftImpulse = 0;
                event.getInput().forwardImpulse = 0;
                event.getInput().jumping = false;
            }
        }

        @SubscribeEvent
        public static void onSpellZoom(ComputeFovModifierEvent event) {
            Player player = event.getPlayer();
            var handler = SpellUtil.getSpellHandler(player);
            event.setNewFovModifier(handler.getZoom());
        }
    }
}
