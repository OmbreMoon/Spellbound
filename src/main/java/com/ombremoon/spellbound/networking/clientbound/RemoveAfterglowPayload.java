package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RemoveAfterglowPayload(int entityId) implements CustomPacketPayload {
    public static final Type<RemoveAfterglowPayload> TYPE =
            new Type<>(CommonClass.customLocation("remove_afterglow"));

    public static final StreamCodec<ByteBuf, RemoveAfterglowPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, RemoveAfterglowPayload::entityId,
            RemoveAfterglowPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
