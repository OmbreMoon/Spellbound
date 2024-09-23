package com.ombremoon.spellbound.networking.clientbound;

import com.mojang.serialization.Codec;
import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientSyncManaPayload(float mana) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientSyncManaPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("client_mana_sync"));

    public static final StreamCodec<ByteBuf, ClientSyncManaPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            ClientSyncManaPayload::mana,
            ClientSyncManaPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}