package com.ombremoon.spellbound.common.content.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;

public interface ISpellEntity extends GeoEntity {

    EntityType<?> entityType();

    void setOwner(@NotNull LivingEntity entity);

    @Nullable
    Entity getOwner();

    void setSpellId(int id);

    int getSpellId();
}
