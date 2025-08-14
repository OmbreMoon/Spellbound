package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.content.spell.ruin.ice.ShatteringCrystalSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;

public class ShatteringCrystal extends SpellEntity<ShatteringCrystalSpell> {
    public final float bobOffs;

    public ShatteringCrystal(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.bobOffs = this.random.nextFloat() * (float) Math.PI * 2.0F;
    }

    @Override
    public void tick() {
        super.tick();
        RandomSource random = this.level().random;
        if (random.nextInt(8) == 0) {
            this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(),
                    this.getY() + 2,
                    this.getZ(),
                    Mth.randomBetween(random, -0.5F, 0.5F) * 0.083333336F,
                    0.05F,
                    Mth.randomBetween(random, -0.5F, 0.5F) * 0.083333336F
            );
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, this::genericController));
    }

    public float getSpin(float partialTick) {
        return (this.tickCount + partialTick) / 20.0F + this.bobOffs;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}
