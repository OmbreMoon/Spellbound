package com.ombremoon.spellbound.common.content.entity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;

public abstract class PortalEntity extends SpellEntity {
    private final Map<UUID, Integer> portalCooldown = new Object2IntOpenHashMap<>();

    public PortalEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        this.portalCooldown.entrySet().removeIf(entry -> entry.getValue() <= this.tickCount);
    }

    public abstract int getPortalCooldown();

    public void addCooldown(LivingEntity entity) {
        this.portalCooldown.put(entity.getUUID(), this.tickCount + this.getPortalCooldown());
    }

    public boolean isOnCooldown(LivingEntity livingEntity) {
        return this.portalCooldown.containsKey(livingEntity.getUUID());
    }
}
