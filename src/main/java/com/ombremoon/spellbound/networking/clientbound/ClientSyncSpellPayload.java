package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientSyncSpellPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientSyncSpellPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("client_spell_sync"));

    public static final StreamCodec<ByteBuf, ClientSyncSpellPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ClientSyncSpellPayload::tag,
            ClientSyncSpellPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
