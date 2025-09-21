package com.ombremoon.spellbound.client.renderer.layer;

import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpellCastModel<T extends AbstractSpell> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T animatable) {
        return CommonClass.customLocation("geo/spell_layer/spell_cast.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return CommonClass.customLocation("textures/spell_layer/spell_cast.png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return null;
    }

}
