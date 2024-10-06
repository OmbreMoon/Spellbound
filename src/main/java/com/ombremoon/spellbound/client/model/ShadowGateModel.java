package com.ombremoon.spellbound.client.model;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.entity.ShadowGate;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ShadowGateModel extends DefaultedEntityGeoModel<ShadowGate> {
    public ShadowGateModel() {
        super(CommonClass.customLocation("shadow_gate"));
    }
}
