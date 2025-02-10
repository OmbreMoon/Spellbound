package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChangeHailLevelPayload(float hailLevel) implements CustomPacketPayload {
    public static final Type<ChangeHailLevelPayload> TYPE = new Type<>(CommonClass.customLocation("change_hail_level"));

    public static final StreamCodec<ByteBuf, ChangeHailLevelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ChangeHailLevelPayload::hailLevel,
            ChangeHailLevelPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

