package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public record UpdateDimensionsPayload(Set<ResourceKey<Level>> keys, boolean add) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateDimensionsPayload> TYPE = new CustomPacketPayload.Type<>(CommonClass.customLocation("update_dimensions"));

    public static final StreamCodec<ByteBuf, UpdateDimensionsPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION).apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), UpdateDimensionsPayload::keys,
            ByteBufCodecs.BOOL, UpdateDimensionsPayload::add,
            UpdateDimensionsPayload::new);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
