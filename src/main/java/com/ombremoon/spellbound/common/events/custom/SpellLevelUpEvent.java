package com.ombremoon.spellbound.common.events.custom;

import com.ombremoon.spellbound.common.magic.api.SpellType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class SpellLevelUpEvent extends LivingEvent {
    private final SpellType<?> spellType;
    private final int level;

    public SpellLevelUpEvent(LivingEntity entity, SpellType<?> spellType, int level) {
        super(entity);
        this.spellType = spellType;
        this.level = level;
    }

    public SpellType<?> getSpellType() {
        return this.spellType;
    }

    public int getLevel() {
        return this.level;
    }
}
