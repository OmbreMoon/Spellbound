package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateFlagPayload(SpellType<?> spellType, int flag) implements CustomPacketPayload {
    public static final Type<UpdateFlagPayload> TYPE = new Type<>(CommonClass.customLocation("update_flag"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateFlagPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SBSpells.SPELL_TYPE_REGISTRY_KEY), UpdateFlagPayload::spellType,
            ByteBufCodecs.INT, UpdateFlagPayload::flag,
            UpdateFlagPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
