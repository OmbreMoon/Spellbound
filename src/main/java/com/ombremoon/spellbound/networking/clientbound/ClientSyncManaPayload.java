package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientSyncManaPayload(double mana) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientSyncManaPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("client_mana_sync"));

    public static final StreamCodec<ByteBuf, ClientSyncManaPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            ClientSyncManaPayload::mana,
            ClientSyncManaPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
