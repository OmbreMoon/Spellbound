package com.ombremoon.spellbound.client.renderer.layer;

import com.ombremoon.spellbound.common.magic.api.AbstractSpell;

public class SpellLayer<T extends AbstractSpell> extends SpellLayerRenderer<T> {
    public SpellLayer() {
        super(new SpellLayerModel<>());
    }
}
