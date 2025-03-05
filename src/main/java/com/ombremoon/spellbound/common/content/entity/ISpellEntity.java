package com.ombremoon.spellbound.common.content.entity;

import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;

public interface ISpellEntity<T extends AbstractSpell> extends GeoEntity {

    EntityType<?> entityType();

    void setOwner(@NotNull LivingEntity entity);

    @Nullable
    Entity getOwner();

    void setSpell(SpellType<?> spellType, int id);

    T getSpell();
}
