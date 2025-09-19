package com.ombremoon.spellbound.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class CircleAroundPositionParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final double xStart;
    private final double yStart;
    private final double zStart;
    private final double radius;
    private final double angularVelocity;
    private final boolean isGlowing;
    private final Particle.LifetimeAlpha lifetimeAlpha;

    public CircleAroundPositionParticle(ClientLevel level, double x, double y, double z, double radius, double angularVelocity, SpriteSet sprites) {
        this(level, x, y, z, radius, angularVelocity, false, LifetimeAlpha.ALWAYS_OPAQUE, sprites);
    }

    CircleAroundPositionParticle(ClientLevel level, double x, double y, double z, double radius, double angularVelocity, boolean isGlowing, Particle.LifetimeAlpha lifetimeAlpha, SpriteSet sprites) {
        super(level, x, y, z);
        this.isGlowing = isGlowing;
        this.lifetimeAlpha = lifetimeAlpha;
        this.sprites = sprites;
        this.setAlpha(lifetimeAlpha.startAlpha());
        this.angularVelocity = angularVelocity;
        this.xStart = x;
        this.yStart = y;
        this.zStart = z;
        this.radius = radius;
        this.xo = x + Math.cos(Math.random()) * this.radius;
        this.yo = y;
        this.zo = z + Math.sin(Math.random()) * this.radius;
        this.x = this.xo;
        this.y = this.yo;
        this.z = this.zo;
        this.quadSize = 0.15F;
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = 0.9F * f;
        this.gCol = 0.9F * f;
        this.bCol = f;
        this.hasPhysics = false;
        this.lifetime = (int)(Math.random() * 10.0) + 30;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.lifetimeAlpha.isOpaque() ? ParticleRenderType.PARTICLE_SHEET_OPAQUE : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public int getLightColor(float partialTick) {
        if (this.isGlowing) {
            return 240;
        } else {
            int i = super.getLightColor(partialTick);
            float f = (float)this.age / (float)this.lifetime;
            f *= f;
            f *= f;
            int j = i & 255;
            int k = i >> 16 & 255;
            k += (int)(f * 15.0F * 16.0F);
            if (k > 240) {
                k = 240;
            }

            return j | k << 16;
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            double angle = this.age * this.angularVelocity;
            this.x = this.xStart + Math.cos(angle) * this.radius;
            this.z = this.zStart + Math.sin(angle) * this.radius;
            float progress = (float) this.age / (float) this.lifetime;
            this.y = this.yStart + (progress * 0.5);
        }
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        this.setAlpha(this.lifetimeAlpha.currentAlphaForAge(this.age, this.lifetime, partialTicks));
        super.render(buffer, renderInfo, partialTicks);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new CircleAroundPositionParticle(level, x, y, z, xSpeed, ySpeed, this.sprites);
        }
    }
}
