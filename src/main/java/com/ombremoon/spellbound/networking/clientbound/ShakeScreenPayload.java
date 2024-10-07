package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ShakeScreenPayload(int duration, float intensity, float maxOffset, int freq) implements CustomPacketPayload {
    public static final Type<ShakeScreenPayload> TYPE =
            new Type<>(CommonClass.customLocation("shake_screen"));

    public static final StreamCodec<ByteBuf, ShakeScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ShakeScreenPayload::duration,
            ByteBufCodecs.FLOAT, ShakeScreenPayload::intensity,
            ByteBufCodecs.FLOAT, ShakeScreenPayload::maxOffset,
            ByteBufCodecs.INT, ShakeScreenPayload::freq,
            ShakeScreenPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
