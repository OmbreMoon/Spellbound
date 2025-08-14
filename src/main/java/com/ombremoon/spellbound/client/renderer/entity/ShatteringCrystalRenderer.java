package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.client.renderer.types.EmissiveOutlineSpellRenderer;
import com.ombremoon.spellbound.common.content.entity.spell.ShatteringCrystal;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class ShatteringCrystalRenderer extends EmissiveOutlineSpellRenderer<ShatteringCrystal> {
    public ShatteringCrystalRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    protected void applyRotations(ShatteringCrystal animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        float f1 = Mth.sin((animatable.tickCount + partialTick) / 10.0F + animatable.bobOffs) * 0.1F + 0.1F;
        poseStack.translate(0.0F, f1 + 0.25F - 0.25F, 0.0F);
        float f3 = animatable.getSpin(partialTick);
        poseStack.mulPose(Axis.YP.rotation(f3));
    }
}
