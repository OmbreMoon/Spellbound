package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CastStartPayload(SpellType<?> spellType, boolean recast) implements CustomPacketPayload {
    public static final Type<CastStartPayload> TYPE = new Type<>(CommonClass.customLocation("cast_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastStartPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SBSpells.SPELL_TYPE_REGISTRY_KEY), CastStartPayload::spellType,
            ByteBufCodecs.BOOL, CastStartPayload::recast,
            CastStartPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
