package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateGlowEffectPayload(int entityId, boolean remove) implements CustomPacketPayload {
    public static final Type<UpdateGlowEffectPayload> TYPE = new Type<>(CommonClass.customLocation("add_glow_effect"));

    public static final StreamCodec<ByteBuf, UpdateGlowEffectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, UpdateGlowEffectPayload::entityId,
            ByteBufCodecs.BOOL, UpdateGlowEffectPayload::remove,
            UpdateGlowEffectPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
