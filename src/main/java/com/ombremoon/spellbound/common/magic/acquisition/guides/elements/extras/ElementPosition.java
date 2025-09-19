package com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

import javax.swing.text.Element;

public record ElementPosition(int xOffset, int yOffset) {
    public static final Codec<ElementPosition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("xOffset", 0).forGetter(ElementPosition::xOffset),
            Codec.INT.optionalFieldOf("yOffset", 0).forGetter(ElementPosition::yOffset)
    ).apply(inst, ElementPosition::new));

    public static ElementPosition getDefault() {
        return new ElementPosition(0, 0);
    }
}
