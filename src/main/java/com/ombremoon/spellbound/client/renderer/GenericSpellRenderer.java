package com.ombremoon.spellbound.client.renderer;

import com.ombremoon.spellbound.client.model.GenericSpellModel;
import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GenericSpellRenderer<T extends SpellEntity> extends GeoEntityRenderer<T> {
    public GenericSpellRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GenericSpellModel<>());
    }
}
