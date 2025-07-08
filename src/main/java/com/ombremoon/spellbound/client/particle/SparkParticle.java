package com.ombremoon.spellbound.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class SparkParticle extends HugeExplosionParticle {
    protected SparkParticle(ClientLevel level, double x, double y, double z, double quadSizeMultiplier, SpriteSet sprites) {
        super(level, x, y, z, quadSizeMultiplier, sprites);
        this.lifetime = 5;
        this.quadSize = 0.15F;
        this.setSpriteFromAge(sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SparkParticle(level, x, y, z, xSpeed, this.sprites);
        }
    }
}
