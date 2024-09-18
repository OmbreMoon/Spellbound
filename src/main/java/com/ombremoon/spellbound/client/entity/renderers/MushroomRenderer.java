package com.ombremoon.spellbound.client.entity.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.client.entity.ModelLayers;
import com.ombremoon.spellbound.client.entity.models.MushroomModel;
import com.ombremoon.spellbound.common.content.entity.custom.MushroomEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class MushroomRenderer extends EntityRenderer<MushroomEntity> {
    private MushroomModel<MushroomEntity> model;

    public MushroomRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new MushroomModel<>(context.bakeLayer(ModelLayers.WILD_MUSHROOM));
    }

    @Override
    public void render(MushroomEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        poseStack.translate(0, -(p_entity.getBbHeight() + 0.5F), 0);
        VertexConsumer consumer = bufferSource.getBuffer(this.model.renderType(getTextureLocation(p_entity)));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.pack(0, false));
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MushroomEntity mushroomEntity) {
        return CommonClass.customLocation("textures/entity/wild_mushroom.png");
    }
}
