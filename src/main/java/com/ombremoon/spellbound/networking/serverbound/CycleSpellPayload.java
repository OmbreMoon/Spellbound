package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CycleSpellPayload() implements CustomPacketPayload {
    public static final Type<CycleSpellPayload> TYPE = new Type<>(CommonClass.customLocation("cycle_spell"));
    public static final StreamCodec<ByteBuf, CycleSpellPayload> CODEC = StreamCodec.unit(new CycleSpellPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
