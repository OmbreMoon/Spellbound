package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PlayAnimationPayload(String animation) implements CustomPacketPayload {
    public static final Type<PlayAnimationPayload> TYPE =
            new Type<>(CommonClass.customLocation("play_animation"));

    public static final StreamCodec<ByteBuf, PlayAnimationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayAnimationPayload::animation,
            PlayAnimationPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
