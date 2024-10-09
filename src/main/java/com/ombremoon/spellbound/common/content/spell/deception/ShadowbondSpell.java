package com.ombremoon.spellbound.common.content.spell.deception;

import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.UnknownNullability;

public class ShadowbondSpell extends AnimatedSpell {
    public static AnimatedSpell.Builder<ShadowbondSpell> createShadowbondBuilder() {
        return createSimpleSpellBuilder().castCondition((context, spell) -> {
            if (context.getSkillHandler().hasSkill(SkillInit.SHADOW_CHAIN.value()) && context.isRecast()) {

            }
            return context.getTarget() != null;
        }).duration(300).fullRecast().shouldPersist();
    }

    private LivingEntity secondTarget = null;

    public ShadowbondSpell() {
        super(SpellInit.SHADOWBOND.get(), createShadowbondBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        LivingEntity livingEntity = context.getTarget();
        if ()
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        return super.saveData(compoundTag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
    }
}