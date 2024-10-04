package com.ombremoon.spellbound.common.content.entity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

import java.util.Map;
import java.util.UUID;

public class ShadowGate extends SpellEntity {
    private static final EntityDataAccessor<Boolean> SPAWNING = SynchedEntityData.defineId(ShadowGate.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ENDING = SynchedEntityData.defineId(ShadowGate.class, EntityDataSerializers.BOOLEAN);
    private final Map<UUID, Integer> portalCooldown = new Object2IntOpenHashMap<>();

    public ShadowGate(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPAWNING, true);
        builder.define(ENDING, false);
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

    public boolean isEnding() {
        return this.entityData.get(ENDING);
    }

    public void setEnding(boolean ending) {
        this.entityData.set(ENDING, ending);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 5, this::shadowGateController));
    }

    private <T extends GeoAnimatable> PlayState shadowGateController(AnimationState<T> data) {
        if (this.tickCount <= 20) {
            data.setAnimation(RawAnimation.begin().thenPlay("spawn"));
        } else if (isEnding()) {
            data.setAnimation(RawAnimation.begin().thenPlay("end"));
        } else {
            data.setAnimation(RawAnimation.begin().thenLoop("idle"));
        }
        return PlayState.CONTINUE;
    }
}
