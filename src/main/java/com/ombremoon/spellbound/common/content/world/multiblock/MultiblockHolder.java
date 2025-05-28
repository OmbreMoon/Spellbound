package com.ombremoon.spellbound.common.content.world.multiblock;

import net.minecraft.resources.ResourceLocation;

public record MultiblockHolder<T extends Multiblock>(ResourceLocation id, T value) {
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof MultiblockHolder<?> multiblockHolder && this.id.equals(multiblockHolder.id);
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}
