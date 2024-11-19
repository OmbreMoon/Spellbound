package com.ombremoon.spellbound.common.content.spell.divine;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.EffectManager;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.buff.events.DamageEvent;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HealingTouchSpell extends AnimatedSpell {
    private static final ResourceLocation ARMOR_MOD = CommonClass.customLocation("oak_blessing_mod");
    private static final ResourceLocation PLAYER_DAMAGE = CommonClass.customLocation("healing_touch_player_damage");

    private LivingEntity caster;
    private int overgrowthStacks = 0;
    private int blessingDuration = 0;

    private static Builder<HealingTouchSpell> createHealingSpell() {
        return createSimpleSpellBuilder(HealingTouchSpell.class)
                .manaCost(50)
                .duration(context -> 300)
                .fullRecast();
    }


    //TODO: Hunger/hp/mana numbers need refining
    public HealingTouchSpell() {
        super(SBSpells.HEALING_TOUCH.get(), createHealingSpell());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        if (context.isRecast() || context.getLevel().isClientSide) return;
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.POST_DAMAGE, PLAYER_DAMAGE, this::onDamagePost);

        context.getCaster().addEffect(new MobEffectInstance(SBEffects.HEALING_TOUCH, getDuration()));

        SkillHolder skills = context.getSkills();
        this.caster = context.getCaster();
        if (skills.hasSkill(SBSkills.NATURES_TOUCH.value())) context.getCaster().heal(2f);

        if (skills.hasSkill(SBSkills.CLEANSING_TOUCH.value())) {
            Collection<MobEffectInstance> effects = context.getCaster().getActiveEffects();
            List<Holder<MobEffect>> harmfulEffects = new ArrayList<>();
            for (MobEffectInstance instance : effects) {
                if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) harmfulEffects.add(instance.getEffect());
            }

            if (harmfulEffects.isEmpty()) return;
            Holder<MobEffect> removed = harmfulEffects.get(context.getCaster().getRandom().nextInt(0, harmfulEffects.size()));
            context.getCaster().removeEffect(removed);
        }

    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        if (context.getLevel().isClientSide) return;
        SkillHolder skills = context.getSkills();
        LivingEntity caster = context.getCaster();
        double maxMana = caster.getAttribute(SBAttributes.MAX_MANA).getValue();

        if (!caster.hasEffect(SBEffects.HEALING_TOUCH)) {
            endSpell();
            return;
        }

        float heal = 0.2f;
        if (skills.hasSkill(SBSkills.HEALING_STREAM.value()))
            heal += (float) maxMana * 0.02f;

        caster.heal(heal);

        if (skills.hasSkill(SBSkills.ACCELERATED_GROWTH.value()) && caster instanceof Player player) {
            player.getFoodData().eat((int) (maxMana * 0.02d), 1f);
        }

        if (skills.hasSkill(SBSkills.TRANQUILITY_OF_WATER.value()))
            context.getSpellHandler().awardMana(4 + (skills.getSpellLevel(getSpellType()) * 0.4f));

        if (skills.hasSkill(SBSkills.OVERGROWTH.value()) && overgrowthStacks < 5) overgrowthStacks++;

        AttributeInstance armor = caster.getAttribute(Attributes.ARMOR);
        if (blessingDuration > 0) blessingDuration--;
        if (blessingDuration <= 0 && armor.hasModifier(ARMOR_MOD)) {
            armor.removeModifier(ARMOR_MOD);
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        context.getCaster().removeEffect(SBEffects.HEALING_TOUCH);
    }

    @Override
    protected boolean shouldTickSpellEffect(SpellContext context) {
        return ticks % 20 == 0;
    }

    private void onDamagePost(DamageEvent.Post event) {
        SkillHolder skills = SpellUtil.getSkillHolder(caster);

        if (event.getEntity().hasEffect(SBEffects.POISON) && skills.hasSkill(SBSkills.CONVALESCENCE.value()))
            caster.heal(0.5f);

        if (!event.getEntity().is(caster)) return;
        if (overgrowthStacks > 0) {
            caster.heal(2f);
            overgrowthStacks--;
        }
        if (skills.hasSkillReady(SBSkills.BLASPHEMY.value())) {
            EffectManager status = event.getEntity().getData(SBData.STATUS_EFFECTS);
            status.increment(EffectManager.Effect.DISEASE, 100);
            addCooldown(SBSkills.BLASPHEMY.value(), 100);
        }
        if (skills.hasSkillReady(SBSkills.OAK_BLESSING.value())) {
            blessingDuration = 200;
            caster.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(
                    ARMOR_MOD,
                    1.15d,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
            addCooldown(SBSkills.OAK_BLESSING.value(), 600);
        }
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        compoundTag.putInt("overgrowth", this.overgrowthStacks);
        compoundTag.putInt("blessing", this.blessingDuration);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag nbt) {
        this.overgrowthStacks = nbt.getInt("overgrowth");
        this.blessingDuration = nbt.getInt("blessing");
    }

}
