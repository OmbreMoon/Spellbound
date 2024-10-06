package com.ombremoon.spellbound.client.renderer.layer;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpellLayerModel<T extends AbstractSpell> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T animatable) {
        return CommonClass.customLocation("geo/spell_layer/" + animatable.getId().getPath() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return CommonClass.customLocation("textures/spell_layer/" + animatable.getId().getPath() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return CommonClass.customLocation("animations/spell_layer/" + animatable.getId().getPath() + ".animation.json");
    }
}
