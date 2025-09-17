package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.client.renderer.types.EmissiveOutlineSpellRenderer;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class SolarRayRenderer extends EmissiveOutlineSpellRenderer<SolarRay> {

    public SolarRayRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    protected void applyRotations(SolarRay animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
//        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        Entity owner = animatable.getOwner();
        if (owner == null) return;
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, owner.yRotO, owner.getYRot())));
    }

    @Override
    public boolean shouldRender(SolarRay spellEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
