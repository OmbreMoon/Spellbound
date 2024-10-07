package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncSkillPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSkillPayload> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("client_skill_sync"));

    public static final StreamCodec<ByteBuf, SyncSkillPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncSkillPayload::tag,
            SyncSkillPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
