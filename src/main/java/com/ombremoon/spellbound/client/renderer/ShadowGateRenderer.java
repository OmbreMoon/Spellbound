package com.ombremoon.spellbound.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.client.model.ShadowGateModel;
import com.ombremoon.spellbound.common.content.entity.ShadowGate;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ShadowGateRenderer extends GeoEntityRenderer<ShadowGate> {
    public ShadowGateRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ShadowGateModel());
    }

    @Override
    public @Nullable RenderType getRenderType(ShadowGate animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.itemEntityTranslucentCull(texture);
    }

    @Override
    protected void applyRotations(ShadowGate animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-animatable.yRotO));
        if (SpellUtil.getSkillHandler((LivingEntity) animatable.getOwner()).hasSkill(SkillInit.GRAVITY_SHIFT.value())) {
            poseStack.translate(0, -0.05F, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(30));
        }
    }
}
