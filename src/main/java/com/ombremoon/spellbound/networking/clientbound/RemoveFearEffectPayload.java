package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RemoveFearEffectPayload() implements CustomPacketPayload {
    public static final Type<RemoveFearEffectPayload> TYPE = new Type<>(CommonClass.customLocation("remove_fear_effect"));

    public static final StreamCodec<ByteBuf, RemoveFearEffectPayload> STREAM_CODEC = StreamCodec.unit(new RemoveFearEffectPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
