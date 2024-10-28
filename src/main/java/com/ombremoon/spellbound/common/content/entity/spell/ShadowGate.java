package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.*;

import java.util.Map;
import java.util.UUID;

public class ShadowGate extends SpellEntity {
    private final Map<UUID, Integer> portalCooldown = new Object2IntOpenHashMap<>();

    public ShadowGate(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
//        discard();
        this.portalCooldown.entrySet().removeIf(entry -> entry.getValue() <= this.tickCount);
    }

    public void addCooldown(LivingEntity entity) {
        this.portalCooldown.put(entity.getUUID(), this.tickCount + 20);
    }

    public boolean isOnCooldown(LivingEntity livingEntity) {
        return this.portalCooldown.containsKey(livingEntity.getUUID());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, this::genericController));
    }
}
