package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetRotationPayload(float xRot, float yRot) implements CustomPacketPayload {
    public static final Type<SetRotationPayload> TYPE = new Type<>(CommonClass.customLocation("manipulate_camera"));

    public static final StreamCodec<ByteBuf, SetRotationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SetRotationPayload::xRot,
            ByteBufCodecs.FLOAT, SetRotationPayload::yRot,
            SetRotationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
