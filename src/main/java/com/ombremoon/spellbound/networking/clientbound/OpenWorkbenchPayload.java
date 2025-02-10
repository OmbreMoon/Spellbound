package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenWorkbenchPayload() implements CustomPacketPayload {
    public static final Type<OpenWorkbenchPayload> TYPE =
            new Type<>(CommonClass.customLocation("open_workbench"));

    public static final StreamCodec<ByteBuf, OpenWorkbenchPayload> STREAM_CODEC = StreamCodec.unit(new OpenWorkbenchPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
