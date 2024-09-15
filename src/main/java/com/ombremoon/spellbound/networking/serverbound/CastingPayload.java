package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CastingPayload(SpellType<?> spellType, int castTime) implements CustomPacketPayload {
    public static final Type<CastingPayload> TYPE = new Type<>(CommonClass.customLocation("casting"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastingPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SpellInit.SPELL_TYPE_REGISTRY_KEY), CastingPayload::spellType,
            ByteBufCodecs.INT, CastingPayload::castTime,
            CastingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
