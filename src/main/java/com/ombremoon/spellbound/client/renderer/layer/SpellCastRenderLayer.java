package com.ombremoon.spellbound.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.util.Color;

public class SpellCastRenderLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final SpellCastRenderer<? extends AbstractSpell> spellCastLayer;

    public SpellCastRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.spellCastLayer = new SpellCastLayer<>();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        this.getParentModel().copyPropertiesTo(spellCastLayer);
        this.spellCastLayer.prepForRender(livingEntity, null, this.getParentModel(), bufferSource, partialTick, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        this.spellCastLayer.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, Color.WHITE.argbInt());
    }
}
