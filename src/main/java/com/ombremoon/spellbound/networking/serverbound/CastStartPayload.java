package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.checkerframework.checker.units.qual.C;

public record CastStartPayload() implements CustomPacketPayload {
    public static final Type<CastStartPayload> TYPE = new Type<>(CommonClass.customLocation("cast_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastStartPayload> STREAM_CODEC = StreamCodec.unit(new CastStartPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
