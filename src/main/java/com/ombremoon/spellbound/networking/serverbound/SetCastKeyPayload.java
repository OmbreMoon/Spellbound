package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetCastKeyPayload(boolean isDown) implements CustomPacketPayload {
    public static final Type<SetCastKeyPayload> TYPE = new Type<>(CommonClass.customLocation("set_cast_key"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetCastKeyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SetCastKeyPayload::isDown,
            SetCastKeyPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
