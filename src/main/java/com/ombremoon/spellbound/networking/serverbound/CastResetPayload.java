package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CastResetPayload(SpellType<?> spellType, boolean recast) implements CustomPacketPayload {
    public static final Type<CastResetPayload> TYPE = new Type<>(CommonClass.customLocation("cast_reset"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastResetPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SBSpells.SPELL_TYPE_REGISTRY_KEY), CastResetPayload::spellType,
            ByteBufCodecs.BOOL, CastResetPayload::recast,
            CastResetPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
