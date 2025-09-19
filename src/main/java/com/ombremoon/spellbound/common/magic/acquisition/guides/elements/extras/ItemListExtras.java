package com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ItemListExtras(int maxRows, int rowGap, int columnGap, int countGap, boolean dropShadow, int textColour) implements IElementExtra {
    public static final Codec<ItemListExtras> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("maxRows", 0).forGetter(ItemListExtras::maxRows),
            Codec.INT.optionalFieldOf("rowGap", 20).forGetter(ItemListExtras::rowGap),
            Codec.INT.optionalFieldOf("columnGap", 45).forGetter(ItemListExtras::columnGap),
            Codec.INT.optionalFieldOf("countGap", 33).forGetter(ItemListExtras::countGap),
            Codec.BOOL.optionalFieldOf("dropShadow", false).forGetter(ItemListExtras::dropShadow),
            Codec.INT.optionalFieldOf("textColour", 0).forGetter(ItemListExtras::textColour)
    ).apply(inst, ItemListExtras::new));

    public static ItemListExtras getDefault() {
        return new ItemListExtras(0, 20, 45, 33, true, 0);
    }
}
