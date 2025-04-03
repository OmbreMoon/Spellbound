package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.client.model.entity.SolarRayModel;
import com.ombremoon.spellbound.client.renderer.SBRenderTypes;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SolarRayRenderer extends GeoEntityRenderer<SolarRay> {
    public SolarRayRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SolarRayModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    protected void applyRotations(SolarRay animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        Entity owner = animatable.getOwner();
        if (owner == null) return;
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, owner.yRotO, owner.getYRot())));
    }

    @Override
    public void defaultRender(PoseStack poseStack, SolarRay animatable, MultiBufferSource bufferSource, @Nullable RenderType renderType, @Nullable VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
//        VertexConsumer vertexConsumer = VertexMultiConsumer.create(bufferSource.getBuffer(SBRenderTypes.heatDistortion(getTextureLocation(animatable))),
//                bufferSource.getBuffer(RenderType.itemEntityTranslucentCull(getTextureLocation(animatable))));
//        Constants.LOG.debug("{}", );
//        Constants.LOG.debug("{}", );
        Entity owner = animatable.getOwner();
        if (Minecraft.getInstance().player.is(owner) && Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT)
            return;

        super.defaultRender(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight);
    }

    @Override
    public void renderFinal(PoseStack poseStack, SolarRay animatable, BakedGeoModel model, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public boolean shouldRender(SolarRay spellEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public @Nullable RenderType getRenderType(SolarRay animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
//        return SBRenderTypes.heatDistortion(texture);
        return RenderType.itemEntityTranslucentCull(texture);
    }
}
