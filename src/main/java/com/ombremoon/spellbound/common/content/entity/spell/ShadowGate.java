package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.PortalEntity;
import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.*;

import java.util.Map;
import java.util.UUID;

public class ShadowGate extends PortalEntity {

    public ShadowGate(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public int getPortalCooldown() {
        return 20;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, this::genericController));
    }
}
