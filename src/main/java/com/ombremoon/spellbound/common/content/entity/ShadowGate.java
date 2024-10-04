package com.ombremoon.spellbound.common.content.entity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;

public class ShadowGate extends SpellEntity {
    private Map<UUID, Integer> portalCooldown = new Object2IntOpenHashMap<>();

    public ShadowGate(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        for (var entry : this.portalCooldown.entrySet()) {
            int i = entry.getValue();
            i--;
            if (i > 0) {
                this.portalCooldown.replace(entry.getKey(), i);
            } else {
                this.portalCooldown.remove(entry.getKey());
            }
        }
    }

    public void addCooldown(LivingEntity entity, int ticks) {
        this.portalCooldown.put(entity.getUUID(), ticks);
    }

    public boolean isOnCooldown(LivingEntity livingEntity) {
        return this.portalCooldown.containsKey(livingEntity.getUUID());
    }
}
