package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetSpellPayload(SpellType<?> spellType) implements CustomPacketPayload {
    public static final Type<SetSpellPayload> TYPE = new Type<>(CommonClass.customLocation("cycle_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetSpellPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SBSpells.SPELL_TYPE_REGISTRY_KEY), SetSpellPayload::spellType,
            SetSpellPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
