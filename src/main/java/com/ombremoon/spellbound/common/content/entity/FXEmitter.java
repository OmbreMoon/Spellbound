package com.ombremoon.spellbound.common.content.entity;

import com.lowdragmc.photon.client.fx.FXEffectExecutor;
import com.ombremoon.spellbound.client.particle.EffectBuilder;
import com.ombremoon.spellbound.client.particle.EffectCache;
import net.minecraft.resources.ResourceLocation;

public interface FXEmitter {

    EffectCache getFXCache();

    default void addFX(EffectBuilder<?> builder) {
        EffectCache cache = this.getFXCache();
        cache.addFX(builder);
    }

    default void handleFXRemoval() {
        EffectCache cache = this.getFXCache();
        cache.handleFXRemoval();
    }

    default void removeFX(ResourceLocation location, boolean removeObjects) {
        EffectCache cache = this.getFXCache();
        cache.removeFX(location, removeObjects);
    }

    default void removeFX(FXEffectExecutor effect, boolean removeObjects) {
        EffectCache cache = this.getFXCache();
        cache.removeFX(effect, removeObjects);
    }
}
