package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HandleAnimationPayload(String playerId, String animation, float castSpeed, boolean stopAnimation) implements CustomPacketPayload {
    public static final Type<HandleAnimationPayload> TYPE =
            new Type<>(CommonClass.customLocation("handle_animation"));

    public static final StreamCodec<ByteBuf, HandleAnimationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, HandleAnimationPayload::playerId,
            ByteBufCodecs.STRING_UTF8, HandleAnimationPayload::animation,
            ByteBufCodecs.FLOAT, HandleAnimationPayload::castSpeed,
            ByteBufCodecs.BOOL, HandleAnimationPayload::stopAnimation,
            HandleAnimationPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
