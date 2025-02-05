package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.PortalEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.*;

public class StormRift extends PortalEntity {
    public static final String EXPLODE = "explode";

    public StormRift(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, this::stormRiftController));
    }

    protected <T extends GeoAnimatable> PlayState stormRiftController(AnimationState<T> data) {
        if (isStarting()) {
//            data.setAnimation(RawAnimation.begin().thenPlay("spawn"));
        } else if (isEnding()) {
            data.setAnimation(RawAnimation.begin().thenPlay("explosion"));
        } else {
            data.setAnimation(RawAnimation.begin().thenLoop("idle"));
        }
        return PlayState.CONTINUE;
    }
}
