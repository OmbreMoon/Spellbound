package com.ombremoon.spellbound.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.util.Color;

public class GenericSpellLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final SpellLayerRenderer<? extends AbstractSpell> spellLayer;

    public GenericSpellLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.spellLayer = new SpellLayer<>();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        var handler = SpellUtil.getSpellHandler(livingEntity);
        for (AbstractSpell spell : handler.getActiveSpells()) {
            if (spell.hasLayer())
                renderSpellLayer(poseStack, bufferSource, livingEntity, spell, packedLight);
        }
    }

    private void renderSpellLayer(PoseStack poseStack, MultiBufferSource bufferSource, T livingEntity, AbstractSpell spell, int packedLight) {
        this.getParentModel().copyPropertiesTo(spellLayer);
        this.spellLayer.prepForRender(livingEntity, spell, this.getParentModel());
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(spellLayer.getTextureLocation(spell)));
        this.spellLayer.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, Color.WHITE.argbInt());
        this.spellLayer.doSpellPostRenderCleanup();
    }
}