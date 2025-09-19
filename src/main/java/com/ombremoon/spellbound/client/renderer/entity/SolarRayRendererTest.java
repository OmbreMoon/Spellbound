package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.util.Color;

public class SolarRayRendererTest extends EntityRenderer<SolarRay> {
    public static final ResourceLocation SOLAR_RAY_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    public SolarRayRendererTest(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SolarRay solarRay, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Entity owner = solarRay.getOwner();
        if (owner == null) return;
        long i = solarRay.level().getGameTime();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, owner.yRotO, owner.getYRot())));
        renderSolarRay(poseStack, bufferSource, partialTick, i, 1, Color.WHITE.argbInt());
        poseStack.popPose();
        super.render(solarRay, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderSolarRay(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, long gameTime, int height, int color) {
        renderSolarRay(poseStack, bufferSource, BeaconRenderer.BEAM_LOCATION, partialTick, 1.0F, gameTime, height, color, 1.0F, 0.25F);
    }

    public static void renderSolarRay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation location, float partialTick, float textureScale, long gameTime, int height, int color, float rayRadius, float glowRadius) {
        poseStack.pushPose();
//        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.pushPose();
        poseStack.translate(0.45F, 0.125F, 1.75F);
        float f = (float)Math.floorMod(gameTime, 40) + partialTick;
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(f * 2.25F - 45.0F));
        renderPart(
                poseStack,
                bufferSource.getBuffer(RenderType.beaconBeam(location, true)),
                FastColor.ARGB32.color(128, color),
                1,
                2,
                0.0F,
                rayRadius,
                rayRadius,
                0.0F,
                -rayRadius,
                0.0F,
                0.0F,
                -rayRadius,
                0.0F,
                1.0F,
                0.0F,
                1.0F
        );
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderPart(PoseStack poseStack, VertexConsumer consumer, int color, int minY, int maxY, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float minU, float maxU, float minV, float maxV) {
        PoseStack.Pose pose = poseStack.last();
        renderQuad(pose, consumer, color, minY, maxY, x4, z4, x3, z4, minU, maxU, minV, maxV);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer, int color, int minY, int maxY, float minX, float minZ, float maxX, float maxZ, float minU, float maxU, float minV, float maxV) {
        addVertex(pose, consumer, color, maxY, minX, minZ, maxU, minV);
        addVertex(pose, consumer, color, minY, minX, minZ, maxU, maxV);
        addVertex(pose, consumer, color, minY, maxX, maxZ, minU, maxV);
        addVertex(pose, consumer, color, maxY, maxX, maxZ, minU, minV);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, int color, float y, float x, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(SolarRay entity) {
        return SOLAR_RAY_LOCATION;
    }
}
