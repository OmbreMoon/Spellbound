package com.ombremoon.spellbound.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.client.model.entity.SolarRayModel;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
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
    public boolean shouldRender(SolarRay spellEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public @Nullable RenderType getRenderType(SolarRay animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
//        return SBRenderTypes.heatDistortion(texture);
        return RenderType.itemEntityTranslucentCull(texture);
    }
}
