package com.ombremoon.spellbound.common.magic;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class SpellType<S extends AbstractSpell> {
    private final ResourceLocation resourceLocation;
    private final SpellFactory<S> factory;

    public SpellType(ResourceLocation resourceLocation, SpellFactory<S> factory) {
        this.factory = factory;
        this.resourceLocation = resourceLocation;
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }

    @Nullable
    public S createSpell() {
        return this.factory.create();
    }

    public interface SpellFactory<S extends AbstractSpell> {
        S create();
    }
}
