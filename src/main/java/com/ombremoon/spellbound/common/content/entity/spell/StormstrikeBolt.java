package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.content.entity.SpellProjectile;
import com.ombremoon.spellbound.common.content.spell.ruin.shock.StormstrikeSpell;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class StormstrikeBolt extends SpellProjectile<StormstrikeSpell> {
    public StormstrikeBolt(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

}
