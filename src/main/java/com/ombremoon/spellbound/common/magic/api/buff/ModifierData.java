package com.ombremoon.spellbound.common.magic.api.buff;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record ModifierData(Holder<Attribute> attribute, AttributeModifier attributeModifier) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ModifierData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE), ModifierData::attribute,
            AttributeModifier.STREAM_CODEC, ModifierData::attributeModifier,
            ModifierData::new
    );

}

