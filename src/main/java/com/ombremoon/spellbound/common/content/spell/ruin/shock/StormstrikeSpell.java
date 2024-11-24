package com.ombremoon.spellbound.common.content.spell.ruin.shock;

import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import net.minecraft.world.level.Level;

public class StormstrikeSpell extends AnimatedSpell {
    public static Builder<StormstrikeSpell> createStormstrikeBuilder() {
        return createSimpleSpellBuilder(StormstrikeSpell.class).manaCost(20);
    }

    public StormstrikeSpell() {
        super(SBSpells.STORMSTRIKE.get(), createStormstrikeBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Level level = context.getLevel();
        if (!level.isClientSide) {
            this.shootProjectile(context, SBEntities.STORMSTRIKE_BOLT.get(), 2.5F, 1.0F);
        }
    }
}
