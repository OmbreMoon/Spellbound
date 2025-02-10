package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SwitchModePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SwitchModePayload> TYPE = new CustomPacketPayload.Type<>(CommonClass.customLocation("switch_mode"));
    public static final StreamCodec<ByteBuf, SwitchModePayload> STREAM_CODEC = StreamCodec.unit(new SwitchModePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
