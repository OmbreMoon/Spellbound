package com.ombremoon.spellbound.client.renderer.spell;

import com.ombremoon.spellbound.common.content.entity.SpellProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class EmissiveSpellProjectileRenderer<T extends SpellProjectile> extends SpellProjectileRenderer<T> {
    public EmissiveSpellProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
