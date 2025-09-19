package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateSpellTicksPayload(int entityId, SpellType<?> spellType, int castId, int ticks) implements CustomPacketPayload {
    public static final Type<UpdateSpellTicksPayload> TYPE = new Type<>(CommonClass.customLocation("update_spell_ticks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSpellTicksPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, UpdateSpellTicksPayload::entityId,
            ByteBufCodecs.registry(SBSpells.SPELL_TYPE_REGISTRY_KEY), UpdateSpellTicksPayload::spellType,
            ByteBufCodecs.INT, UpdateSpellTicksPayload::castId,
            ByteBufCodecs.INT, UpdateSpellTicksPayload::ticks,
            UpdateSpellTicksPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
