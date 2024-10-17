package com.ombremoon.spellbound.common.content.effects;

import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.spellbound.common.content.spell.ruin.shock.StormstrikeSpell;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.api.SpellModifier;
import com.ombremoon.spellbound.common.magic.api.ModifierType;
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
                spell.addTimedModifier(owner, SpellModifier.CHARGED_ATMOSPHERE, 160);
        }
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        int ownerId = livingEntity.getData(SBData.STORMSTRIKE_OWNER);
        Entity entity = livingEntity.level().getEntity(ownerId);
        float damage = 3F;
        if (entity instanceof LivingEntity owner) {
            var skills = SpellUtil.getSkillHolder(owner);
            StormstrikeSpell spell = SBSpells.STORMSTRIKE.get().createSpell();
            float potency = spell.getModifier(ModifierType.POTENCY, owner);
            damage *= potency;

            if (skills.hasSkill(SBSkills.SHOCK_FACTOR.value()))
                damage += (float) (livingEntity.getData(SBData.MANA) * 0.05F);

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

            if (livingEntity.hurt(BoxUtil.sentinelDamageSource(livingEntity.level(), SBDamageTypes.RUIN_SHOCK, entity), damage)) {
                livingEntity.setData(SBData.STORMSTRIKE_FLAG, false);
                if (livingEntity.isDeadOrDying()) {
                    if (skills.hasSkill(SBSkills.STORM_SHARD.value()) && owner instanceof Player player)
                        player.addItem(new ItemStack(SBItems.STORM_SHARD.get()));

                    if (skills.hasSkill(SBSkills.SUPERCHARGE.value()))
                        spell.addTimedModifier(owner, SpellModifier.SUPERCHARGE, 200);
                }
            }
        }
        return super.applyEffectTick(livingEntity, amplifier);
    }


    @Override
    public void onMobHurt(LivingEntity livingEntity, int amplifier, DamageSource damageSource, float amount) {
        int ownerId = livingEntity.getData(SBData.STORMSTRIKE_OWNER);
        Entity entity = livingEntity.level().getEntity(ownerId);
        if (entity instanceof LivingEntity owner) {
            var handler = SpellUtil.getSpellHandler(owner);
            var skills = SpellUtil.getSkillHolder(owner);
            StormstrikeSpell spell = SBSpells.STORMSTRIKE.get().createSpell();

            if (skills.hasSkill(SBSkills.REFRACTION.value()) && damageSource.is(SBDamageTypes.RUIN_SHOCK) && damageSource.getEntity() != null && damageSource.getEntity().is(owner) && livingEntity.getData(SBData.STORMSTRIKE_FLAG)) {
                handler.awardMana(20 + skills.getSpellLevel(spell.getSpellType()) * 2);
                livingEntity.setData(SBData.STORMSTRIKE_FLAG, true);
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
