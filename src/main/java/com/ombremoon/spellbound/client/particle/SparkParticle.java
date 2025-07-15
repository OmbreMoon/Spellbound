package com.ombremoon.spellbound.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class SparkParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected SparkParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z);
        this.gravity = -0.1F;
        this.friction = 0.9F;
        this.sprites = sprites;
        this.xd = xSpeed + (Math.random() * 2.0 - 1.0) * 0.05F;
        this.yd = ySpeed + (Math.random() * 2.0 - 1.0) * 0.05F;
        this.zd = zSpeed + (Math.random() * 2.0 - 1.0) * 0.05F;
        this.lifetime = 5;
        this.quadSize = 0.15F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SparkParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
