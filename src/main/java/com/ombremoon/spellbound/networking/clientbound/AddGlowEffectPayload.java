package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AddGlowEffectPayload(int entityId) implements CustomPacketPayload {
    public static final Type<AddGlowEffectPayload> TYPE =
            new Type<>(CommonClass.customLocation("add_glow_effect"));

    public static final StreamCodec<ByteBuf, AddGlowEffectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AddGlowEffectPayload::entityId,
            AddGlowEffectPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
