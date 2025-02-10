package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RemoveGlowEffectPayload(int entityId) implements CustomPacketPayload {
    public static final Type<RemoveGlowEffectPayload> TYPE =
            new Type<>(CommonClass.customLocation("remove_glow_effect"));

    public static final StreamCodec<ByteBuf, RemoveGlowEffectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, RemoveGlowEffectPayload::entityId,
            RemoveGlowEffectPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
