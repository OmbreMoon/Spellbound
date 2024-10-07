package com.ombremoon.spellbound.common.magic.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface SpellDataType<T> {
    StreamCodec<? super RegistryFriendlyByteBuf, T> codec();

    default SpellDataKey<T> createDataKey(int id) {
        return new SpellDataKey<>(id, this);
    }

    T copy(T value);

    static <T> SpellDataType<T> forValueType(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return (ForValueType<T>) () -> streamCodec;
    }

    interface ForValueType<T> extends SpellDataType<T> {
        @Override
        default T copy(T value) {
            return value;
        }
    }
}
