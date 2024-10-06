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
        this.portalCooldown.entrySet().removeIf(entry -> entry.getValue() <= this.tickCount);
    }

    public void addCooldown(LivingEntity entity) {
        this.portalCooldown.put(entity.getUUID(), this.tickCount + 20);
    }

    public boolean isOnCooldown(LivingEntity livingEntity) {
        return this.portalCooldown.containsKey(livingEntity.getUUID());
    }

    public boolean isEnding() {
        byte b0 = this.entityData.get(ID_FLAGS);
        return (b0 & 1) != 0;
    }

    public void setEnding(boolean ending) {
        this.setFlag(1, ending);
    }

    //CHECK
    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (this.getOwner() != null)
            this.setYRot(this.getOwner().getYRot());
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
