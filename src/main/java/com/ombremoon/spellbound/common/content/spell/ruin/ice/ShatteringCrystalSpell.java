package com.ombremoon.spellbound.common.content.spell.ruin.ice;

import com.ombremoon.spellbound.common.content.entity.spell.ShatteringCrystal;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class ShatteringCrystalSpell extends AnimatedSpell {
    public static Builder<ShatteringCrystalSpell> createShatteringCrystalBuild() {
        return createSimpleSpellBuilder(ShatteringCrystalSpell.class)
                .duration(300)
                .manaCost(20)
                .baseDamage(5)
                .castCondition((context, shatteringCrystalSpell) -> {
                    if (context.getTarget() instanceof ShatteringCrystal crystal && crystal.getOwner().is(context.getCaster())) {
                        return true;
                    }
                    return shatteringCrystalSpell.hasValidSpawnPos();
                })
                .noShift(context -> context.getTarget() instanceof ShatteringCrystal crystal && crystal.getOwner().is(context.getCaster()));
    }

    private int crystalID;
    private boolean primed;
    private int primeTick = 10;

    public ShatteringCrystalSpell() {
        super(SBSpells.SHATTERING_CRYSTAL.get(), createShatteringCrystalBuild());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            LivingEntity caster = context.getCaster();
            var skills = context.getSkills();
            if (context.getTarget() instanceof ShatteringCrystal crystal && crystal.getOwner().is(caster)) {
                this.primeCrystal(context, crystal);
            } else {
                ShatteringCrystal crystal = this.summonEntity(context, SBEntities.SHATTERING_CRYSTAL.get());
                if (crystal != null)
                    this.crystalID = crystal.getId();
            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        if (this.primed) {
            if (this.primeTick > 0) {
                this.primeTick--;
            } else {
                this.explodeCrystal(context);
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            LivingEntity caster = context.getCaster();
            var skills = context.getSkills();
            ShatteringCrystal crystal = this.getCrystal(context);
            if (crystal != null)
                crystal.setEndTick(5);

        }
    }

    @Override
    public boolean shouldRender(SpellContext context) {
        return context.getTarget() instanceof ShatteringCrystal crystal && crystal.getOwner().is(context.getCaster());
    }

    private void primeCrystal(SpellContext context, ShatteringCrystal crystal) {
        ShatteringCrystalSpell spell = crystal.getSpell();
        if (spell != null) {
            endSpell();
            spell.primed = true;
            spell.primeTick = 10;
        }
    }

    private void explodeCrystal(SpellContext context) {
        Level level = context.getLevel();
        var skills = context.getSkills();
        ShatteringCrystal crystal = this.getCrystal(context);
        if (crystal != null) {
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, crystal.getBoundingBox().inflate(4));
            for (Entity entity : entities) {
                if (skills.hasSkill(SBSkills.CHAOTIC_SHATTER) && entity instanceof ShatteringCrystal crystal1) {
                    ShatteringCrystalSpell spell = crystal1.getSpell();
                    spell.primed = true;
                    spell.primeTick = 10;
                }
            }

            if (skills.hasSkill(SBSkills.CRYSTAL_ECHO)) {
                this.setRemainingTicks(200);
            } else {
                this.endSpell();
            }
        }

    }

    private ShatteringCrystal getCrystal(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.crystalID);
        return entity instanceof ShatteringCrystal crystal ? crystal : null;
    }
}
