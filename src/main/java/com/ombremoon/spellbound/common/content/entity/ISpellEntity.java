package com.ombremoon.spellbound.common.content.entity;

import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;

public interface ISpellEntity<T extends AbstractSpell> extends GeoEntity {

    default Entity getEntity() {
        return (Entity) this;
    }

    EntityType<?> entityType();

    void setOwner(@NotNull Entity entity);

    @Nullable
    Entity getOwner();

    void setSpell(@NotNull AbstractSpell spell);

    T getSpell();

    boolean isInitialized();
}
