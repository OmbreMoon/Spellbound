package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record UpdateTreePayload(boolean reset, List<Skill> added, Set<ResourceLocation> removed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateTreePayload> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("update_tree"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTreePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UpdateTreePayload::reset,
            ByteBufCodecs.registry(SBSkills.SKILL_REGISTRY_KEY).apply(ByteBufCodecs.list()), UpdateTreePayload::added,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), UpdateTreePayload::removed,
            UpdateTreePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
