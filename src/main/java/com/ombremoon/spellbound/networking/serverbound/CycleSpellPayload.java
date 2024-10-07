package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CycleSpellPayload(SpellType<?> spellType) implements CustomPacketPayload {
    public static final Type<CycleSpellPayload> TYPE = new Type<>(CommonClass.customLocation("cycle_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CycleSpellPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SpellInit.SPELL_TYPE_REGISTRY_KEY), CycleSpellPayload::spellType,
            CycleSpellPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
