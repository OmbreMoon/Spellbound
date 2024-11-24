package com.ombremoon.spellbound.common.content.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISpellEntity {

    void setOwner(@NotNull LivingEntity entity);

    @Nullable
    Entity getOwner();

    void setSpellId(int id);

    int getSpellId();
}
