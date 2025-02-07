package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.client.KeyBinds;
import com.ombremoon.spellbound.client.gui.CastModeOverlay;
import com.ombremoon.spellbound.client.gui.SpellSelectScreen;
import com.ombremoon.spellbound.client.renderer.blockentity.SummonPortalRenderer;
import com.ombremoon.spellbound.client.renderer.entity.*;
import com.ombremoon.spellbound.client.renderer.spell.*;
import com.ombremoon.spellbound.client.renderer.layer.GenericSpellLayer;
import com.ombremoon.spellbound.client.shader.SBShaders;
import com.ombremoon.spellbound.common.content.world.hailstorm.ClientHailstormData;
import com.ombremoon.spellbound.common.content.world.hailstorm.HailstormSavedData;
import com.ombremoon.spellbound.common.init.SBBlockEntities;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.init.SBEffects;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.buff.events.MouseInputEvent;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
            event.register(KeyBinds.SELECT_SPELL_BINDING);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(SBEntities.MUSHROOM.get(), GenericSpellRenderer::new);
            event.registerEntityRenderer(SBEntities.SHADOW_GATE.get(), ShadowGateRenderer::new);
            event.registerEntityRenderer(SBEntities.SOLAR_RAY.get(), SolarRayRenderer::new);
            event.registerEntityRenderer(SBEntities.STORMSTRIKE_BOLT.get(), EmissiveSpellProjectileRenderer::new);
            event.registerEntityRenderer(SBEntities.STORM_RIFT.get(), StormRiftRenderer::new);
            event.registerEntityRenderer(SBEntities.STORM_CLOUD.get(), StormCloudRenderer::new);
            event.registerEntityRenderer(SBEntities.STORM_BOLT.get(), StormBoltRenderer::new);
            event.registerEntityRenderer(SBEntities.CYCLONE.get(), CycloneRenderer::new);
            event.registerEntityRenderer(SBEntities.HAIL.get(), HailRenderer::new);

            event.registerEntityRenderer(SBEntities.LIVING_SHADOW.get(), LivingShadowRenderer::new);
            event.registerEntityRenderer(SBEntities.VALKYR.get(), GenericLivingEntityRenderer::new);

            event.registerBlockEntityRenderer(SBBlockEntities.SUMMON_PORTAL.get(), SummonPortalRenderer::new);
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
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player != null) {
                SpellHandler handler = SpellUtil.getSpellHandler(player);
                if (KeyBinds.SWITCH_MODE_BINDING.consumeClick()) {
                    handler.switchMode();
                    player.displayClientMessage(Component.literal("Switched to " + (handler.inCastMode() ? "Cast mode" : "Normal mode")), true);
                    PayloadHandler.switchMode();
                }
                if (KeyBinds.SELECT_SPELL_BINDING.consumeClick()) {
                    Screen screen = minecraft.screen;
                    if (handler.inCastMode() && !handler.equippedSpellSet.isEmpty()) {
                        minecraft.setScreen(new SpellSelectScreen());
                    } else {
                        minecraft.setScreen(screen);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onMouseInputPre(InputEvent.MouseButton.Pre event) {
            Player player = Minecraft.getInstance().player;
            if (player != null)
                SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.PRE_MOUSE_INPUT, new MouseInputEvent.Pre(player, event));
        }

        @SubscribeEvent
        public static void onMouseInputPost(InputEvent.MouseButton.Post event) {
            Player player = Minecraft.getInstance().player;
            if (player != null)
                SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.POST_MOUSE_INPUT, new MouseInputEvent.Post(player, event));
        }

        @SubscribeEvent
        public static void onMovementInput(MovementInputUpdateEvent event) {
            var handler = SpellUtil.getSpellHandler(event.getEntity());
            if (event.getEntity().hasEffect(SBEffects.ROOTED) || event.getEntity().hasEffect(SBEffects.STUNNED) || handler.isStationary()) {
                event.getInput().leftImpulse = 0;
                event.getInput().forwardImpulse = 0;
                event.getInput().jumping = false;
            }
        }

        @SubscribeEvent
        public static void onWeatherRender(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
                ClientHailstormData data = (ClientHailstormData) HailstormSavedData.get(Minecraft.getInstance().level);
                data.renderHailstorm(event);
            }
        }

        @SubscribeEvent
        public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
            ClientHailstormData data = (ClientHailstormData) HailstormSavedData.get(Minecraft.getInstance().level);
            data.renderHailstormFog(event);
        }

        @SubscribeEvent
        public static void onSpellZoom(ComputeFovModifierEvent event) {
            Player player = event.getPlayer();
            var handler = SpellUtil.getSpellHandler(player);
            event.setNewFovModifier(handler.getZoom());
        }

        @SubscribeEvent
        public static void onRenderLevelLast(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
                SBShaders.setupPoseStack(event.getPoseStack());
                SBShaders.processShaders();
            }
        }
    }
}
