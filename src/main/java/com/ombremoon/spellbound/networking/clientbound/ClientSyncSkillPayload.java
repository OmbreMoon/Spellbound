package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientSyncSkillPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientSyncSkillPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("client_skill_sync"));

    public static final StreamCodec<ByteBuf, ClientSyncSkillPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ClientSyncSkillPayload::tag,
            ClientSyncSkillPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
