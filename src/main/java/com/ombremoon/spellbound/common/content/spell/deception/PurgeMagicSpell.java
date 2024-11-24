package com.ombremoon.spellbound.common.content.spell.deception;

import com.ombremoon.spellbound.common.init.SBEffects;
import com.ombremoon.spellbound.common.init.SBItems;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.RadialSpell;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.api.buff.SpellModifier;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class PurgeMagicSpell extends AnimatedSpell implements RadialSpell {
    private static Builder<PurgeMagicSpell> createPurgeMagicBuilder() {
        return createSimpleSpellBuilder(PurgeMagicSpell.class)
                .duration(context -> {
                    int flag = context.getFlag();
                    if (flag == 1) return 200;
                    return 10;
                })
                .castCondition((context, purgeMagicSpell) -> {
                    if (context.getFlag() == 0)
                        return context.getTarget() instanceof LivingEntity;
                    return true;
                }).fullRecast();
    }

    public PurgeMagicSpell() {
        super(SBSpells.PURGE_MAGIC.get(), createPurgeMagicBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        var skills = context.getSkills();
        int flag = context.getFlag();
        if (flag == 1) {
            caster.addEffect(new MobEffectInstance(SBEffects.COUNTER_MAGIC, 200, 0, false ,false));
            if (skills.hasSkill(SBSkills.CLEANSE.value()))
                caster.getActiveEffects().stream().filter(instance -> !instance.getEffect().value().isBeneficial()).forEach(instance -> caster.removeEffect(instance.getEffect()));

            if (skills.hasSkill(SBSkills.AVERSION.value()))
                log("SOMETHING");
        } else {
            List<LivingEntity> targets = new ObjectArrayList<>();
            if (skills.hasSkill(SBSkills.RADIO_WAVES.value())) {
                targets.addAll(level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(3), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            } else {
                targets.add((LivingEntity) context.getTarget());
            }

            for (LivingEntity target : targets) {
                var targetHandler = SpellUtil.getSpellHandler(target);
                var activeSpells = targetHandler.getActiveSpells();
                targetHandler.getBuffs().forEach(skillBuff -> removeSkillBuff(target, skillBuff.getSkill()));
                for (AbstractSpell spell : activeSpells) {
                    spell.endSpell();
                }
                target.getActiveEffects().stream().filter(instance -> instance.getEffect().value().isBeneficial()).forEach(instance -> caster.removeEffect(instance.getEffect()));

                if (skills.hasSkill(SBSkills.DOMINANT_MAGIC.value()))
                    target.addEffect(new MobEffectInstance(SBEffects.SILENCED, 200, 0, false, false));

                if (skills.hasSkill(SBSkills.RESIDUAL_DISRUPTION.value())) {
                    addSkillBuff(
                            target,
                            SBSkills.RESIDUAL_DISRUPTION.value(),
                            BuffCategory.HARMFUL,
                            SkillBuff.SPELL_MODIFIER,
                            SpellModifier.RESIDUAL_DISRUPTION,
                            -1
                    );
                }

                if (skills.hasSkill(SBSkills.UNFOCUSED.value()))
                    addSkillBuff(
                            target,
                            SBSkills.UNFOCUSED.value(),
                            BuffCategory.HARMFUL,
                            SkillBuff.SPELL_MODIFIER,
                            SpellModifier.UNFOCUSED,
                            200
                    );

                if (skills.hasSkill(SBSkills.MAGIC_POISONING.value()))
                    targetHandler.consumeMana(Math.max(20 * activeSpells.size(), 20));

                if (skills.hasSkill(SBSkills.NULLIFICATION.value())) {
                    List<ItemStack> itemSlots = new ObjectArrayList<>();
                    target.getAllSlots().forEach(itemSlots::add);
                    int randSlot = target.getRandom().nextInt(0, itemSlots.size());
                    ItemStack stack = itemSlots.get(randSlot);
                    if (stack.isEnchanted()) {
                        var enchantments = stack.getAllEnchantments(target.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)).keySet().stream().toList();
                        int randEnchant = target.getRandom().nextInt(0, enchantments.size());
                        stack.enchant(enchantments.get(randEnchant), 0);
                    }
                }

                if (skills.hasSkillReady(SBSkills.EXPUNGE.value()) && context.hasCatalyst(SBItems.FOOL_SHARD.get())) {
                    int randSpell = target.getRandom().nextInt(0, targetHandler.getSpellList().size());
                    SpellType<?> spellType = targetHandler.getSpellList().stream().toList().get(randSpell);
                    targetHandler.removeSpell(spellType);
                    addCooldown(SBSkills.EXPUNGE.value(), 24000);
                }
            }
        }
    }
}
