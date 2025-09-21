package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateChoicePayload(SpellType<?> spellType, Skill skill) implements CustomPacketPayload {
    public static final Type<UpdateChoicePayload> TYPE = new Type<>(CommonClass.customLocation("update_choice"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateChoicePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SBSpells.SPELL_TYPE_REGISTRY_KEY), UpdateChoicePayload::spellType,
            ByteBufCodecs.registry(SBSkills.SKILL_REGISTRY_KEY), UpdateChoicePayload::skill,
            UpdateChoicePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
