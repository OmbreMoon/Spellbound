package com.ombremoon.spellbound.common.content.entity;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface ISpellEntity {

    void setOwner(@NotNull LivingEntity entity);
}
