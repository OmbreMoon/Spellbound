package com.ombremoon.spellbound.client.renderer.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.client.model.GenericSpellModel;
import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GenericSpellRenderer<T extends SpellEntity> extends GeoEntityRenderer<T> {
    public GenericSpellRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GenericSpellModel<>());
    }

    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-animatable.yRotO));
    }
}
