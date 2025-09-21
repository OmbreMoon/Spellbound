package com.ombremoon.spellbound.client.particle;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FXEffectExecutor;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class EffectBuilder<T extends FXEffectExecutor> {
    protected final ResourceLocation location;

    public EffectBuilder(ResourceLocation location) {
        this.location = location;
    }

    public abstract T build();

    public abstract void buildAndStart();

    public static class Block extends EffectBuilder<BlockEffectExecutor> {
        private final BlockPos blockPos;
        private Vec3 offset = Vec3.ZERO;
        private Vec3 rotation = Vec3.ZERO;
        private Vec3 scale = new Vec3(1, 1, 1);
        private int delay;
        private boolean forcedDeath;
        private boolean allowMulti;
        private boolean checkState;

        public Block(ResourceLocation location, BlockPos blockPos) {
            super(location);
            this.blockPos = blockPos;
        }

        @Override
        public BlockEffectExecutor build() {
            Level level = Minecraft.getInstance().level;
            if (level != null && level.isLoaded(this.blockPos)) {
                var fx = FXHelper.getFX(this.location);
                if (fx != null) {
                    var effect = new BlockEffectExecutor(fx, level, blockPos);
                    var offset = this.offset;
                    var rotation = this.rotation;
                    var scale = this.scale;
                    effect.setOffset(offset.x, offset.y, offset.z);
                    effect.setRotation(rotation.x, rotation.y, rotation.z);
                    effect.setScale(scale.x, scale.y, scale.z);
                    effect.setDelay(this.delay);
                    effect.setForcedDeath(this.forcedDeath);
                    effect.setAllowMulti(this.allowMulti);
                    effect.setCheckState(this.checkState);
                    return effect;
                }
            }

            return null;
        }

        @Override
        public void buildAndStart() {
            var effect = this.build();
            if (effect != null)
                effect.start();
        }

        public static Block of(ResourceLocation effect, BlockPos blockPos) {
            return new Block(effect, blockPos);
        }

        public Block setOffset(double x, double y, double z) {
            this.offset = new Vec3(x, y, z);
            return this;
        }

        public Block setRotation(double x, double y, double z) {
            this.rotation = new Vec3(x, y, z);
            return this;
        }

        public Block setScale(double x, double y, double z) {
            this.scale = new Vec3(x, y, z);
            return this;
        }

        public Block setDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public Block setForcedDeath(boolean forcedDeath) {
            this.forcedDeath = forcedDeath;
            return this;
        }

        public Block setAllowMulti(boolean allowMulti) {
            this.allowMulti = allowMulti;
            return this;
        }

        public Block setCheckState(boolean checkState) {
            this.checkState = checkState;
            return this;
        }
    }

    public static class Entity extends EffectBuilder<EntityEffectExecutor> {

        public Entity(ResourceLocation location) {
            super(location);
        }

        @Override
        public EntityEffectExecutor build() {
            return null;
        }

        @Override
        public void buildAndStart() {

        }
    }
}
