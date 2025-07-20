package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CreateParticlesPayload(ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) implements CustomPacketPayload {
    public static final Type<CreateParticlesPayload> TYPE = new Type<>(CommonClass.customLocation("create_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateParticlesPayload> STREAM_CODEC = StreamCodec.ofMember(
            CreateParticlesPayload::write, CreateParticlesPayload::new
    );

    public CreateParticlesPayload(RegistryFriendlyByteBuf byteBuf) {
        this(ParticleTypes.STREAM_CODEC.decode(byteBuf), byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        ParticleTypes.STREAM_CODEC.encode(buffer, this.particle);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        buffer.writeDouble(this.xSpeed);
        buffer.writeDouble(this.ySpeed);
        buffer.writeDouble(this.zSpeed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
