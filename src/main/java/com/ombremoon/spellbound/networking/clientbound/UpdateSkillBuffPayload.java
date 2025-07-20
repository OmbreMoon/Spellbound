package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.main.CommonClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateSkillBuffPayload(int entityId, SkillBuff<?> skillBuff, int duration, boolean removeBuff) implements CustomPacketPayload {
    public static final Type<UpdateSkillBuffPayload> TYPE = new Type<>(CommonClass.customLocation("update_skill_buff"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSkillBuffPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, UpdateSkillBuffPayload::entityId,
            SkillBuff.STREAM_CODEC, UpdateSkillBuffPayload::skillBuff,
            ByteBufCodecs.INT, UpdateSkillBuffPayload::duration,
            ByteBufCodecs.BOOL, UpdateSkillBuffPayload::removeBuff,
            UpdateSkillBuffPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
