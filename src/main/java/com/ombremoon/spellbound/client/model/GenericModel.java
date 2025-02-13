package com.ombremoon.spellbound.client.model;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.content.entity.ISpellEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GenericModel<T extends ISpellEntity<?>> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return CommonClass.customLocation("geo/entity/" + getName(animatable) + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return CommonClass.customLocation("textures/entity/" + getName(animatable) + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return CommonClass.customLocation("animations/entity/" + getName(animatable) + ".animation.json");
    }

    protected String getName(T animatable) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(animatable.entityType()).getPath();
    }
}
