package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.content.spell.ruin.shock.StormRiftSpell;
import com.ombremoon.spellbound.common.init.SBSpells;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.*;

import java.util.List;

public class StormBolt extends SpellEntity<StormRiftSpell> {
    public StormBolt(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        List<Entity> list = this.level()
                .getEntities(
                        this,
                        new AABB(this.getX() - 5.0, this.getY() - 3.0, this.getZ() - 5.0, this.getX() + 5.0, this.getY() + 6.0 + 3.0, this.getZ() + 5.0),
                        Entity::isAlive
                );

        if (this.handler != null) {
            StormRiftSpell spell = this.getSpell();
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingEntity) {
                for (Entity entity : list) {
                    entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
                    if (entity.getRemainingFireTicks() == 0)
                        entity.igniteForSeconds(8.0F);

                    if (entity instanceof LivingEntity target && spell != null)
                        spell.hurt(livingEntity, target, DamageTypes.LIGHTNING_BOLT, 5.0F);
                }
            }
        }

        if (this.tickCount >= 15)
            this.discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, this::stormBoltController));
    }

    protected <T extends GeoAnimatable> PlayState stormBoltController(AnimationState<T> data) {
        if (!this.isRemoved()) {
            data.setAnimation(RawAnimation.begin().thenPlay("spawn"));
        }
        return PlayState.CONTINUE;
    }
}
