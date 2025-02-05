package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.client.renderer.spell.GenericSpellRenderer;
import com.ombremoon.spellbound.common.content.entity.spell.StormRift;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class StormRiftRenderer extends GenericSpellRenderer<StormRift> {
    public StormRiftRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    protected void applyRotations(StormRift animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
    }
}
