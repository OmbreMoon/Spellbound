package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBItems;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.ModifierData;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.util.RandomUtil;

public class MysticArmorSpell extends AnimatedSpell {
    private static final ResourceLocation PRE_DAMAGE = CommonClass.customLocation("mystic_armor_pre_damage");
    private static final ResourceLocation POST_DAMAGE = CommonClass.customLocation("mystic_armor_post_damage");
    private static final ResourceLocation ARCANE_VENGEANCE = CommonClass.customLocation("arcane_vengeance");
    private static final ResourceLocation PURSUIT = CommonClass.customLocation("pursuit");
    private static final ResourceLocation CRYSTALLINE_ARMOR = CommonClass.customLocation("crystalline_armor");

    private static Builder<MysticArmorSpell> createMysticArmorBuilder() {
        return createSimpleSpellBuilder(MysticArmorSpell.class)
                .mastery(SpellMastery.ADEPT)
                .duration(1200)
                .manaCost(28)
                .fullRecast();
    }

    public MysticArmorSpell() {
        super(SBSpells.MYSTIC_ARMOR.get(), createMysticArmorBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        LivingEntity caster = context.getCaster();
        var skills = context.getSkills();
        addEventBuff(
                caster,
                SBSkills.MYSTIC_ARMOR.value(),
                BuffCategory.BENEFICIAL,
                SpellEventListener.Events.PRE_DAMAGE,
                PRE_DAMAGE,
                pre -> {
                    if (isSpellDamage(pre.getSource())) {
                        float f = 0.15F + 0.03F * Math.min(10, context.getSkills().getPathLevel(SpellPath.TRANSFIGURATION));
                        pre.setNewDamage(pre.getOriginalDamage() * f);
                    }

                    if (skills.hasSkill(SBSkills.COMBAT_PERCEPTION.value()) && isPhysicalDamage(pre.getSource()) && RandomUtil.percentChance(0.1))
                        pre.setNewDamage(0);

                    if (skills.hasSkillReady(SBSkills.SOUL_RECHARGE.value()) && context.hasCatalyst(SBItems.SOUL_SHARD.get()) && caster.getHealth() - pre.getNewDamage() < caster.getMaxHealth() * 0.1F) {
                        pre.setNewDamage(0);
                        caster.setHealth(caster.getMaxHealth());
                        context.useCatalyst(SBItems.SOUL_SHARD.get());
                        addCooldown(SBSkills.SOUL_RECHARGE, 3600);
                    } else if (skills.hasSkillReady(SBSkills.ELDRITCH_INTERVENTION.value()) && caster.getHealth() - pre.getNewDamage() < caster.getMaxHealth() * 0.2F) {
                        caster.heal(caster.getMaxHealth() / 2 - caster.getHealth());
                        addCooldown(SBSkills.ELDRITCH_INTERVENTION, 2400);
                    }
                });
        addEventBuff(
                caster,
                SBSkills.MYSTIC_ARMOR.value(),
                BuffCategory.BENEFICIAL,
                SpellEventListener.Events.POST_DAMAGE,
                POST_DAMAGE,
                post -> {
                    Entity entity = post.getSource().getEntity();
                    if (entity instanceof LivingEntity living) {
                        if (skills.hasSkill(SBSkills.EQUILIBRIUM.value()))
                            hurt(living, post.getSource(), caster.getMaxHealth() * 0.1F);

                        if (skills.hasSkill(SBSkills.PLANAR_DEFLECTION.value()) && isPhysicalDamage(post.getSource()))
                            hurt(living, post.getSource(), post.getNewDamage() * 0.3F);
                    }
                });
        if (skills.hasSkill(SBSkills.ARCANE_VENGEANCE.value())) {
            addEventBuff(
                    caster,
                    SBSkills.ARCANE_VENGEANCE.value(),
                    BuffCategory.BENEFICIAL,
                    SpellEventListener.Events.BLOCK,
                    ARCANE_VENGEANCE,
                    blockEvent -> addSkillBuff(
                            caster,
                            SBSkills.ARCANE_VENGEANCE.value(),
                            BuffCategory.BENEFICIAL,
                            SkillBuff.ATTRIBUTE_MODIFIER,
                            new ModifierData(Attributes.ATTACK_DAMAGE, new AttributeModifier(ARCANE_VENGEANCE, 1.15F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                            200));
        }

        if (skills.hasSkill(SBSkills.PURSUIT.value()))
            addSkillBuff(
                    caster,
                    SBSkills.PURSUIT.value(),
                    BuffCategory.BENEFICIAL,
                    SkillBuff.ATTRIBUTE_MODIFIER,
                    new ModifierData(Attributes.MOVEMENT_SPEED, new AttributeModifier(PURSUIT, 1.15F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)));

        if (skills.hasSkill(SBSkills.CRYSTALLINE_ARMOR.value()))
            addSkillBuff(
                    caster,
                    SBSkills.CRYSTALLINE_ARMOR.value(),
                    BuffCategory.BENEFICIAL,
                    SkillBuff.ATTRIBUTE_MODIFIER,
                    new ModifierData(Attributes.ARMOR, new AttributeModifier(CRYSTALLINE_ARMOR, 1.25F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)));

        context.getLevel()
                .playSeededSound(
                        null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ARMOR_EQUIP_NETHERITE, caster.getSoundSource(), 1.0F, 1.0F, caster.getRandom().nextLong()
                );
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        var skills = context.getSkills();

        if (!level.isClientSide) {
            if (skills.hasSkill(SBSkills.SUBLIME_BEACON.value()))
                caster.heal(caster.getArmorValue() * 0.25F);
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        LivingEntity caster = context.getCaster();
        removeSkillBuff(caster, SBSkills.MYSTIC_ARMOR.value());
        removeSkillBuff(caster, SBSkills.ARCANE_VENGEANCE.value());
        removeSkillBuff(caster, SBSkills.PURSUIT.value());
        removeSkillBuff(caster, SBSkills.CRYSTALLINE_ARMOR.value());
    }

    @Override
    protected boolean shouldTickSpellEffect(SpellContext context) {
        return this.ticks % 60 == 0;
    }
}
