package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SwitchModePayload(boolean castMode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SwitchModePayload> TYPE = new CustomPacketPayload.Type<>(CommonClass.customLocation("switch_mode"));
    public static final StreamCodec<ByteBuf, SwitchModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SwitchModePayload::castMode,
            SwitchModePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
