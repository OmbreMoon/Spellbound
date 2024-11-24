package com.ombremoon.spellbound.client.model.entity;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.entity.spell.Hail;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HailModel extends GeoModel<Hail> {

    @Override
    public ResourceLocation getModelResource(Hail animatable) {
        return CommonClass.customLocation("geo/entity/hail.geo.json");

    }

    @Override
    public ResourceLocation getTextureResource(Hail animatable) {
        return CommonClass.customLocation("textures/entity/hail.png");

    }

    @Override
    public ResourceLocation getAnimationResource(Hail animatable) {
        return CommonClass.customLocation("animations/entity/hail.animation.json");

    }
}
