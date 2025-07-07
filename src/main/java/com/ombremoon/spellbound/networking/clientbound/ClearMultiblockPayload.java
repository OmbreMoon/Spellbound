package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClearMultiblockPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<ClearMultiblockPayload> TYPE =
            new Type<>(CommonClass.customLocation("clear_multiblock"));

    public static final StreamCodec<ByteBuf, ClearMultiblockPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ClearMultiblockPayload::tag,
            ClearMultiblockPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
