package com.ombremoon.spellbound.common.content.spell.ruin.shock;

import com.ombremoon.spellbound.common.content.entity.spell.StormstrikeBolt;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        if (!level.isClientSide) {
            StormstrikeBolt bolt = new StormstrikeBolt(context.getLevel(), context.getCaster(), this);
            bolt.setSpellId(this.getId());
            bolt.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, 2.5F, 1.0F);
            level.addFreshEntity(bolt);
        }
    }
}
