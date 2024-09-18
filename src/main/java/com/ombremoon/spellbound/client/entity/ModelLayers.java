package com.ombremoon.spellbound.client.entity;

import com.ombremoon.spellbound.CommonClass;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class ModelLayers {
    public static final ModelLayerLocation WILD_MUSHROOM = register("wild_mushroom");

    private static ModelLayerLocation register(String name) {
        return new ModelLayerLocation(CommonClass.customLocation(name), "main");
    }
}
