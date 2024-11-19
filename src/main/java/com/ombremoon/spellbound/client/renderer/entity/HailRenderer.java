package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.client.model.GenericSpellModel;
import com.ombremoon.spellbound.client.model.entity.HailModel;
import com.ombremoon.spellbound.client.renderer.spell.GenericSpellRenderer;
import com.ombremoon.spellbound.client.renderer.spell.OutlineSpellRenderer;
import com.ombremoon.spellbound.common.content.entity.spell.Cyclone;
import com.ombremoon.spellbound.common.content.entity.spell.Hail;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HailRenderer extends GeoEntityRenderer<Hail> {
    public HailRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HailModel());
    }

    @Override
    protected void applyRotations(Hail animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        poseStack.mulPose(Axis.XP.rotationDegrees(ageInTicks * 10));
        poseStack.mulPose(Axis.ZP.rotationDegrees(ageInTicks * 10));
    }

    @Override
    public boolean shouldRender(Hail spellEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public @Nullable RenderType getRenderType(Hail animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.itemEntityTranslucentCull(texture);
    }
}
