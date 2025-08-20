package com.ombremoon.spellbound.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.client.model.blockentity.ValkyrStatueModel;
import com.ombremoon.spellbound.common.content.block.AbstractExtendedBlock;
import com.ombremoon.spellbound.common.content.block.entity.ValkyrBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ValkyrStatueRenderer extends GeoBlockRenderer<ValkyrBlockEntity> {
    public ValkyrStatueRenderer(BlockEntityRendererProvider.Context context) {
        super(new ValkyrStatueModel());
    }

    @Override
    public void render(ValkyrBlockEntity animatable, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!animatable.getBlockState().getValue(AbstractExtendedBlock.CENTER))
            return;

        super.render(animatable, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
