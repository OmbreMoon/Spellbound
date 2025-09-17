package com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.GuideItemList;

public record ItemListExtras(int maxRows, int rowGap, int columnGap, int countGap) {
    public static final MapCodec<ItemListExtras> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.optionalFieldOf("maxRows", 0).forGetter(ItemListExtras::maxRows),
            Codec.INT.optionalFieldOf("rowGap", 20).forGetter(ItemListExtras::rowGap),
            Codec.INT.optionalFieldOf("columnGap", 45).forGetter(ItemListExtras::columnGap),
            Codec.INT.optionalFieldOf("countGap", 33).forGetter(ItemListExtras::countGap)
    ).apply(inst, ItemListExtras::new));

    public static ItemListExtras getDefault() {
        return new ItemListExtras(0, 20, 45, 33);
    }
}
