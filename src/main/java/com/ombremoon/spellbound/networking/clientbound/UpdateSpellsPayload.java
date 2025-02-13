package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateSpellsPayload(boolean isRecast, int castId, boolean forceReset) implements CustomPacketPayload {
    public static final Type<UpdateSpellsPayload> TYPE = new Type<>(CommonClass.customLocation("update_spells"));

    public static final StreamCodec<ByteBuf, UpdateSpellsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UpdateSpellsPayload::isRecast,
            ByteBufCodecs.INT, UpdateSpellsPayload::castId,
            ByteBufCodecs.BOOL, UpdateSpellsPayload::forceReset,
            UpdateSpellsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
