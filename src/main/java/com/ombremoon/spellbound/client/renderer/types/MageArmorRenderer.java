package com.ombremoon.spellbound.client.renderer.types;

import com.ombremoon.spellbound.client.model.MageArmorModel;
import com.ombremoon.spellbound.common.content.item.MageArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MageArmorRenderer extends GeoArmorRenderer<MageArmorItem> {
    public MageArmorRenderer() {
        super(new MageArmorModel());
    }
}
