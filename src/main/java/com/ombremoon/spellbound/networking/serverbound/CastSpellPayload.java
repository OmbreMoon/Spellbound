package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CastSpellPayload(SpellType<?> spellType) implements CustomPacketPayload {
    public static final Type<CastSpellPayload> TYPE = new Type<>(CommonClass.customLocation("cast_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastSpellPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SpellInit.SPELL_TYPE_REGISTRY_KEY), CastSpellPayload::spellType,
            CastSpellPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
