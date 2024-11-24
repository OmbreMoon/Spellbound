package com.ombremoon.spellbound.common.events.custom;

import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class SpellCastEvent extends LivingEvent {
    private final AbstractSpell spell;
    private final SpellContext context;

    public SpellCastEvent(LivingEntity entity, AbstractSpell spell, SpellContext context) {
        super(entity);
        this.spell = spell;
        this.context = context;
    }

    public AbstractSpell getSpell() {
        return this.spell;
    }

    public SpellContext getContext() {
        return this.context;
    }
}
