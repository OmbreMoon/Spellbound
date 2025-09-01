package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

public record ElementPosition(int xOffset, int yOffset) {
    public static final Codec<ElementPosition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("xOffset").forGetter(ElementPosition::xOffset),
            Codec.INT.fieldOf("yOffset").forGetter(ElementPosition::yOffset)
    ).apply(inst, ElementPosition::new));

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(xOffset);
        buffer.writeInt(yOffset);
    }

    public static ElementPosition read(FriendlyByteBuf buffer) {
        return new ElementPosition(buffer.readInt(), buffer.readInt());
    }
}
