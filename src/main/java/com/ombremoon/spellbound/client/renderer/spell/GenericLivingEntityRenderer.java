package com.ombremoon.spellbound.client.renderer.spell;

import com.ombremoon.spellbound.client.model.LivingModel;
import com.ombremoon.spellbound.common.content.entity.SmartSpellEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GenericLivingEntityRenderer<T extends SmartSpellEntity> extends GeoEntityRenderer<T> {

    public GenericLivingEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LivingModel<>());
    }

    @Override
    protected float getDeathMaxRotation(T animatable) {
        return 0.0F;
    }

    @Override
    public int getPackedOverlay(T animatable, float u, float partialTick) {
        return OverlayTexture.pack(OverlayTexture.u(u),
                OverlayTexture.v(false));
    }
}
