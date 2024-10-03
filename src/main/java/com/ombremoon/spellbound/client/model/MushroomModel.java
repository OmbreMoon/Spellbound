package com.ombremoon.spellbound.client.model;

import com.ombremoon.spellbound.CommonClass;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class MushroomModel<T extends GeoEntity> extends DefaultedEntityGeoModel<T> {
    public MushroomModel() {
        super(CommonClass.customLocation("wild_mushroom"));
    }
}
