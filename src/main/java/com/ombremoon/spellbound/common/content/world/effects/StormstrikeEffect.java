package com.ombremoon.spellbound.common.content.world.effects;

import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.spellbound.common.content.spell.ruin.shock.StormstrikeSpell;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.api.buff.SpellModifier;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.util.RandomUtil;

import javax.swing.*;

public class StormstrikeEffect extends SBEffect {
    public StormstrikeEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectStarted(LivingEntity livingEntity, int amplifier) {
        int ownerId = livingEntity.getData(SBData.STORMSTRIKE_OWNER);
        Entity entity = livingEntity.level().getEntity(ownerId);
        if (entity instanceof LivingEntity owner) {
            var skills = SpellUtil.getSkillHolder(owner);
            StormstrikeSpell spell = SBSpells.STORMSTRIKE.get().createSpell();

            if (skills.hasSkill(SBSkills.CHARGED_ATMOSPHERE.value()))
                spell.addSkillBuff(
                        owner,
                        SBSkills.CHARGED_ATMOSPHERE.value(),
                        BuffCategory.BENEFICIAL,
                        SkillBuff.SPELL_MODIFIER,
                        SpellModifier.CHARGED_ATMOSPHERE,
                        160);
        }
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        int ownerId = livingEntity.getData(SBData.STORMSTRIKE_OWNER);
        Entity entity = livingEntity.level().getEntity(ownerId);
        float damage = 2F;
        if (entity instanceof LivingEntity owner) {
            var skills = SpellUtil.getSkillHolder(owner);
            StormstrikeSpell spell = SBSpells.STORMSTRIKE.get().createSpellWithData(owner);

            if (skills.hasSkill(SBSkills.SHOCK_FACTOR.value()))
                damage += (float) (livingEntity.getData(SBData.MANA) * 0.01F);

            if (skills.hasSkill(SBSkills.PURGE.value()) && SpellUtil.isSummon(livingEntity) && !SpellUtil.isSummonOf(livingEntity, owner))
                damage += (float) (livingEntity.getData(SBData.MANA) * 0.1F);

            if (skills.hasSkill(SBSkills.DISCHARGE.value())) {
                ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
                if (!itemStack.isEmpty() && RandomUtil.percentChance(0.2)) {
                    if (livingEntity instanceof Player player) {
                        player.drop(itemStack, true);
                    } else {
                        itemStack.shrink(1);
                    }
                }
            }

            if (skills.hasSkill(SBSkills.PULSATION.value()) && RandomUtil.percentChance(0.1))
                livingEntity.addEffect(new MobEffectInstance(SBEffects.STUNNED, 20, 0, false, false));

            if (spell.hurt(livingEntity, damage)) {
                if (livingEntity.isDeadOrDying()) {
                    if (skills.hasSkillReady(SBSkills.STORM_SHARD) && owner instanceof Player player) {
                        player.addItem(new ItemStack(SBItems.STORM_SHARD.get()));
                        skills.getCooldowns().addCooldown(SBSkills.STORM_SHARD.value(), 600);
                    }

                    if (skills.hasSkill(SBSkills.SUPERCHARGE.value()))
                        spell.addSkillBuff(
                                owner,
                                SBSkills.SUPERCHARGE.value(),
                                BuffCategory.BENEFICIAL,
                                SkillBuff.SPELL_MODIFIER,
                                SpellModifier.SUPERCHARGE,
                                200);
                }
            }
        } else {
            livingEntity.hurt(BoxUtil.damageSource(livingEntity.level(), SBDamageTypes.RUIN_SHOCK, null), damage);
        }
        return true;
    }


    @Override
    public void onMobHurt(LivingEntity livingEntity, int amplifier, DamageSource damageSource, float amount) {
        int ownerId = livingEntity.getData(SBData.STORMSTRIKE_OWNER);
        Entity entity = livingEntity.level().getEntity(ownerId);
        if (entity instanceof LivingEntity owner) {
            var handler = SpellUtil.getSpellHandler(owner);
            var skills = SpellUtil.getSkillHolder(owner);
            StormstrikeSpell spell = SBSpells.STORMSTRIKE.get().createSpell();

            if (skills.hasSkill(SBSkills.REFRACTION.value()) && damageSource.is(SBDamageTypes.RUIN_SHOCK) && damageSource.getEntity() != null && damageSource.getEntity().is(owner))
                handler.awardMana(20 + skills.getSpellLevel(spell.getSpellType()) * 2);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
