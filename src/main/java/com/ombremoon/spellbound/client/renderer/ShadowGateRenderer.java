package com.ombremoon.spellbound.client.renderer;

import com.ombremoon.spellbound.client.model.ShadowGateModel;
import com.ombremoon.spellbound.common.content.entity.ShadowGate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ShadowGateRenderer extends GeoEntityRenderer<ShadowGate> {
    public ShadowGateRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ShadowGateModel());
    }

    @Override
    public @Nullable RenderType getRenderType(ShadowGate animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.itemEntityTranslucentCull(texture);
    }
}
