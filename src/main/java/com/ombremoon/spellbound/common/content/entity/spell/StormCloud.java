package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.content.spell.ruin.shock.StormRiftSpell;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;

public class StormCloud extends SpellEntity<StormRiftSpell> {
    private int lightningTimer = 60;

    public StormCloud(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.lightningTimer > 0) {
                this.lightningTimer--;
            } else {
                this.lightningTimer = this.getLightningCooldown();
                double x = this.getX() + (random.nextDouble() * 8 - 4);
                double y = this.getY() - 7;
                double z = this.getZ() + (random.nextDouble() * 8 - 4);
                BlockPos blockPos = BlockPos.containing(x, y, z);
                if (this.handler != null) {
                    StormRiftSpell spell = handler.getSpell(SBSpells.STORM_RIFT.get());
                    if (spell != null) {
                        spell.summonEntity(SpellContext.simple(SBSpells.STORM_RIFT.get(), this.handler.caster), SBEntities.STORM_BOLT.get(), Vec3.atBottomCenterOf(blockPos));
                        this.level()
                                .playSound(
                                        this,
                                        this.getOnPos(),
                                        SoundEvents.LIGHTNING_BOLT_THUNDER,
                                        SoundSource.WEATHER,
                                        10000.0F,
                                        0.8F + this.random.nextFloat() * 0.2F
                                );
                        this.level()
                                .playSound(
                                        this,
                                        this.getOnPos(),
                                        SoundEvents.LIGHTNING_BOLT_IMPACT,
                                        SoundSource.WEATHER,
                                        2.0F,
                                        0.5F + this.random.nextFloat() * 0.2F
                                );
                    }
                }
            }
        }
    }

    private int getLightningCooldown() {
        return 40 + this.random.nextInt(40);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, this::genericController));
    }
}
