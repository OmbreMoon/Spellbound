package com.ombremoon.spellbound.client.renderer;

import com.ombremoon.spellbound.client.model.MushroomModel;
import com.ombremoon.spellbound.common.content.entity.MushroomEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MushroomRenderer extends GeoEntityRenderer<MushroomEntity> {
    public MushroomRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MushroomModel<>());
    }
}
