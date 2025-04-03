package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChargeOrChannelPayload(boolean isChargingOrChannelling) implements CustomPacketPayload {
    public static final Type<ChargeOrChannelPayload> TYPE = new Type<>(CommonClass.customLocation("charge_or_channel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargeOrChannelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ChargeOrChannelPayload::isChargingOrChannelling,
            ChargeOrChannelPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
