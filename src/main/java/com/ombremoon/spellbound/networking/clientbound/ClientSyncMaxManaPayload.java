package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientSyncMaxManaPayload(float mana) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientSyncMaxManaPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("client_max_mana_sync"));

    public static final StreamCodec<ByteBuf, ClientSyncMaxManaPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            ClientSyncMaxManaPayload::mana,
            ClientSyncMaxManaPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
