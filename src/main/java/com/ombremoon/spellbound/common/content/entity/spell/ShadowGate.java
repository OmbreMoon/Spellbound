package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.PortalEntity;
import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.content.spell.transfiguration.ShadowGateSpell;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.*;

import java.util.Map;
import java.util.UUID;

public class ShadowGate extends PortalEntity<ShadowGateSpell> {
    private static final EntityDataAccessor<Boolean> SHIFTED = SynchedEntityData.defineId(ShadowGate.class, EntityDataSerializers.BOOLEAN);

    public ShadowGate(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHIFTED, false);
    }

    public void shift() {
        this.entityData.set(SHIFTED, true);
    }

    public boolean isShifted() {
        return this.entityData.get(SHIFTED);
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
