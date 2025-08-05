package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.content.spell.summon.WildMushroomSpell;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class WildMushroom extends SpellEntity<WildMushroomSpell> {

    public WildMushroom(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

}
