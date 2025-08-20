package com.ombremoon.spellbound.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.client.event.custom.SBRenderEvent;
import com.ombremoon.spellbound.client.renderer.layer.SpellCastRenderer;
import com.ombremoon.spellbound.client.renderer.layer.SpellLayerRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.neoforge.common.NeoForge;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class ClientEventFactory {

    public static boolean fireSpellLayerPreRender(SpellLayerRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        return !NeoForge.EVENT_BUS.post(new SBRenderEvent.SpellLayer.Pre(renderer, poseStack, model, bufferSource, partialTick, packedLight)).isCanceled();
    }

    public static void fireSpellLayerPostRender(SpellLayerRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        NeoForge.EVENT_BUS.post(new SBRenderEvent.SpellLayer.Post(renderer, poseStack, model, bufferSource, partialTick, packedLight));
    }

    public static void fireSpellLayerCompileRenderLayers(SpellLayerRenderer<?> renderer) {
        NeoForge.EVENT_BUS.post(new SBRenderEvent.SpellLayer.CompileRenderLayers(renderer));
    }

    public static boolean fireSpellCastPreRender(SpellCastRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        return !NeoForge.EVENT_BUS.post(new SBRenderEvent.SpellCast.Pre(renderer, poseStack, model, bufferSource, partialTick, packedLight)).isCanceled();
    }

    public static void fireSpellCastPostRender(SpellCastRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        NeoForge.EVENT_BUS.post(new SBRenderEvent.SpellCast.Post(renderer, poseStack, model, bufferSource, partialTick, packedLight));
    }

    public static void fireSpellCastCompileRenderLayers(SpellCastRenderer<?> renderer) {
        NeoForge.EVENT_BUS.post(new SBRenderEvent.SpellCast.CompileRenderLayers(renderer));
    }
}
