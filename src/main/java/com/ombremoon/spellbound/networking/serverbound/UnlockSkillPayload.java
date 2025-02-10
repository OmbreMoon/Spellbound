package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UnlockSkillPayload(Skill skill) implements CustomPacketPayload {
    public static final Type<UnlockSkillPayload> TYPE = new Type<>(CommonClass.customLocation("unlock_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockSkillPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(SBSkills.SKILL_REGISTRY_KEY), UnlockSkillPayload::skill,
            UnlockSkillPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
