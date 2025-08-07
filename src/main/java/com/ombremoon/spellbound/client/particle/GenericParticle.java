package com.ombremoon.spellbound.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class GenericParticle extends TextureSheetParticle {

    public GenericParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y - 0.125, z, xSpeed, ySpeed, zSpeed);
        float f = this.random.nextFloat() * 0.1F + 0.2F;
        this.rCol = f;
        this.gCol = f;
        this.bCol = f;
        this.setSize(0.02F, 0.02F);
        this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.5F);
        this.xd *= 0.02F;
        this.yd *= 0.02F;
        this.zd *= 0.02F;
        this.lifetime = (int)(20.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.99;
            this.yd *= 0.99;
            this.zd *= 0.99;
        }
    }

    public static class SludgeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SludgeProvider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            GenericParticle genericParticle = new GenericParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            genericParticle.pickSprite(this.sprite);
            genericParticle.setColor(0.137F, 0.075F, 0.188F);
            return genericParticle;
        }
    }

    public static class MushroomSpore implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public MushroomSpore(SpriteSet sprites) {
            this.sprite = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            GenericParticle genericParticle = new GenericParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            genericParticle.pickSprite(this.sprite);
            boolean color = level.random.nextBoolean();
            genericParticle.setColor(color ? 0.247F : 0.137F, color ? 1.0F : 0.075F, color ? 0.227F : 0.188F);
            return genericParticle;
        }
    }
}
