package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.client.model.GenericModel;
import com.ombremoon.spellbound.common.content.entity.spell.HealingBlossom;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class HealingBlossomRenderer extends GeoEntityRenderer<HealingBlossom> {
    public HealingBlossomRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GenericModel<>());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void render(HealingBlossom entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, Math.sin(entity.tickCount & 360), 0);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    protected void applyRotations(HealingBlossom animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-animatable.yRotO));
    }

    @Override
    public boolean shouldRender(HealingBlossom livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
