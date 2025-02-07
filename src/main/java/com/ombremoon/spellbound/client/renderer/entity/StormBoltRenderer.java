package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.sentinellib.api.Easing;
import com.ombremoon.spellbound.client.renderer.spell.EmissiveOutlineSpellRenderer;
import com.ombremoon.spellbound.client.renderer.spell.OutlineSpellRenderer;
import com.ombremoon.spellbound.common.content.entity.spell.StormBolt;
import com.ombremoon.spellbound.common.content.entity.spell.StormCloud;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class StormBoltRenderer extends EmissiveOutlineSpellRenderer<StormBolt> {
    public StormBoltRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    protected void applyRotations(StormBolt animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
//        float easing = Math.min(2, Easing.QUAD_IN.easing(2, (float) animatable.tickCount / 100));
        poseStack.scale(2, 2, 2);
    }
}
