package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.client.renderer.types.OutlineSpellRenderer;
import com.ombremoon.spellbound.common.content.entity.spell.ShadowGate;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;

public class ShadowGateRenderer extends OutlineSpellRenderer<ShadowGate> {
    public ShadowGateRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    protected void applyRotations(ShadowGate animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        if (animatable.getOwner() != null) {
            if (SpellUtil.getSkills((LivingEntity) animatable.getOwner()).hasSkill(SBSkills.GRAVITY_SHIFT.value())) {
                poseStack.translate(0, -0.05F, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(30));
            }
        }
    }
}
