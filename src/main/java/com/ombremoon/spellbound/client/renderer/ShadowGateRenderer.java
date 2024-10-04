package com.ombremoon.spellbound.client.renderer;

import com.ombremoon.spellbound.client.model.MushroomModel;
import com.ombremoon.spellbound.common.content.entity.MushroomEntity;
import com.ombremoon.spellbound.common.content.entity.ShadowGate;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ShadowGateRenderer extends GeoEntityRenderer<ShadowGate> {
    public ShadowGateRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MushroomModel<>());
    }
}
