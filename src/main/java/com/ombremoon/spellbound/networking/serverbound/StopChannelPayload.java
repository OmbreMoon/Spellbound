package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StopChannelPayload() implements CustomPacketPayload {
    public static final Type<StopChannelPayload> TYPE = new Type<>(CommonClass.customLocation("stop_channel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StopChannelPayload> CODEC = StreamCodec.unit(new StopChannelPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
