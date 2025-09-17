package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record PlayerMovementPayload(Movement movement, float forwardImpulse, float leftImpulse, float yRot) implements CustomPacketPayload {
    public static final Type<PlayerMovementPayload> TYPE = new Type<>(CommonClass.customLocation("player_movement"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerMovementPayload> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(Movement.class), PlayerMovementPayload::movement,
            ByteBufCodecs.FLOAT, PlayerMovementPayload::forwardImpulse,
            ByteBufCodecs.FLOAT, PlayerMovementPayload::leftImpulse,
            ByteBufCodecs.FLOAT, PlayerMovementPayload::yRot,
            PlayerMovementPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Movement {
        MOVE, ROTATE
    }
}
