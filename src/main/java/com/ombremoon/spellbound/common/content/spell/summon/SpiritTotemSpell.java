package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.common.content.entity.living.TotemSpiritEntity;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;

import java.util.Set;

public class SpiritTotemSpell extends SummonSpell {

    public static Builder<SpiritTotemSpell> createSpiritTotemBuilder() {
        return createSummonBuilder(SpiritTotemSpell.class).manaCost(50).duration(600);
    }

    public SpiritTotemSpell() {
        super(SpellInit.SPIRIT_TOTEM.get(), createSpiritTotemBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);

        if (context.isRecast() && context.getSkills().hasSkill(SkillInit.TWIN_SPIRITS.value())) return;
        addSpiritTotem(context);
    }

    @Override
    protected void onSpellRecast(SpellContext context) {
        SpellHandler handler = context.getSpellHandler();
        SkillHandler skills = context.getSkills();

        if (!skills.hasSkill(SkillInit.TWIN_SPIRITS.value())) {
            handler.getActiveSpells(getSpellType()).getFirst().endSpell();
        } else {
            SpiritTotemSpell twinSpell = (SpiritTotemSpell) handler.getActiveSpells(getSpellType()).get(1);
            if (handler.getActiveSpells(getSpellType()).size() > 2) {
                handler.getActiveSpells(getSpellType()).getFirst().endSpell();
            }

            TotemSpiritEntity entity = (TotemSpiritEntity) context.getLevel().getEntity(twinSpell.getSummons().iterator().next());
            entity.setTwin(true);

            TotemSpiritEntity totem = addSpiritTotem(context);
            totem.setTwin(true);
            if (!entity.isCatForm()) totem.switchForm();
        }
    }

    private TotemSpiritEntity addSpiritTotem(SpellContext context) {
//        Set<TotemSpiritEntity> mobs = addMobs(context, EntityInit.TOTEM_SPIRIT.get(), 1);
//        if (mobs == null || mobs.isEmpty()) {
//            endSpell();
//            //TODO: refund mana
//            return null;
//        }
//
//        TotemSpiritEntity spirit = mobs.iterator().next();
//        spirit.initSpell(context.getSkills(), this);
//        return spirit;
        endSpell();
        return null;
    }
}
