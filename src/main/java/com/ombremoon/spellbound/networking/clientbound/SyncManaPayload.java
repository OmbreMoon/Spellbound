package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncManaPayload(double mana) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncManaPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("client_mana_sync"));

    public static final StreamCodec<ByteBuf, SyncManaPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            SyncManaPayload::mana,
            SyncManaPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
