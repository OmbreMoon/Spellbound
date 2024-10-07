package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncSpellPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSpellPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("client_spell_sync"));

    public static final StreamCodec<ByteBuf, SyncSpellPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncSpellPayload::tag,
            SyncSpellPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
