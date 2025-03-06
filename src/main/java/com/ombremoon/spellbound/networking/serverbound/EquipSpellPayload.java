package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EquipSpellPayload(SpellType<?> spellType, boolean equip) implements CustomPacketPayload {
    public static final Type<EquipSpellPayload> TYPE = new Type<>(CommonClass.customLocation("equip_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipSpellPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SBSpells.SPELL_TYPE_REGISTRY_KEY), EquipSpellPayload::spellType,
            ByteBufCodecs.BOOL, EquipSpellPayload::equip,
            EquipSpellPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
