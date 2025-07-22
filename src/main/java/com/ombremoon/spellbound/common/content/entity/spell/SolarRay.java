package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.content.spell.ruin.fire.SolarRaySpell;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;

public class SolarRay extends SpellEntity<SolarRaySpell> {
    private static final EntityDataAccessor<Boolean> SUNSHINE = SynchedEntityData.defineId(SolarRay.class, EntityDataSerializers.BOOLEAN);

    public SolarRay(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SUNSHINE, false);
    }

    public boolean hasSunshine() {
        return this.entityData.get(SUNSHINE);
    }

    public void addSunshine() {
        this.entityData.set(SUNSHINE, true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, this::genericController));
    }
}
