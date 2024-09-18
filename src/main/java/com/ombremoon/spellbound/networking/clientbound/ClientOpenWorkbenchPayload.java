package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientOpenWorkbenchPayload() implements CustomPacketPayload {
    public static final Type<ClientOpenWorkbenchPayload> TYPE =
            new Type<>(CommonClass.customLocation("open_workbench"));

    public static final StreamCodec<ByteBuf, ClientOpenWorkbenchPayload> CODEC = StreamCodec.unit(new ClientOpenWorkbenchPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
