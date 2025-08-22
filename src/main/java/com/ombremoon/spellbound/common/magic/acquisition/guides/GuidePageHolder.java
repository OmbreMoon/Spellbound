package com.ombremoon.spellbound.common.magic.acquisition.guides;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record GuideBookHolder(ResourceLocation id, GuideBookPage page) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GuideBookHolder> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, GuideBookHolder::id,
            GuideBookPage.STREAM_CODEC, GuideBookHolder::page,
            GuideBookHolder::new
    );

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        return obj instanceof GuideBookHolder holder && holder.id().equals(this.id);
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
