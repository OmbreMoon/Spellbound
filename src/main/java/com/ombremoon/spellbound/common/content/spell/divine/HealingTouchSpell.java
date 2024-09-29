package com.ombremoon.spellbound.common.content.spell.divine;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.client.event.SpellCastEvents;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.StatusHandler;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.events.PlayerDamageEvent;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class HealingTouchSpell extends AnimatedSpell {
    private static final ResourceLocation ARMOR_MOD = CommonClass.customLocation("oak_blessing_mod");
    private static final UUID PLAYER_DAMAGE = UUID.fromString("9dae5689-3b32-49c8-9635-0d68d17a9907");

    private Player caster;
    private int overgrowthStacks = 0;
    private int blessingCooldown = 0;

    private static Builder<AnimatedSpell> createHealingSpell() {
        return new Builder<>().setManaCost(10).setDuration(300);
    }


    //TODO: Whole spell needs testing, Needs recasts handled
    public HealingTouchSpell() {
        super(SpellInit.HEALING_TOUCH.get(), createHealingSpell());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        if (context.isRecast()) return;

        SkillHandler skills = context.getSkillHandler();
        this.caster = context.getPlayer();
        context.getPlayer().addEffect(new MobEffectInstance(EffectInit.HEALING_TOUCH, getDuration(skills)));
        if (skills.hasSkill(SkillInit.NATURES_TOUCH.value())) context.getPlayer().heal(2f);

        if (skills.hasSkill(SkillInit.CLEANSING_TOUCH.value())) {
            Collection<MobEffectInstance> effects = context.getPlayer().getActiveEffects();
            List<Holder<MobEffect>> harmfulEffects = new ArrayList<>();
            for (MobEffectInstance instance : effects) {
                if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) harmfulEffects.add(instance.getEffect());
            }

            Holder<MobEffect> removed = harmfulEffects.get(context.getPlayer().getRandom().nextInt(0, harmfulEffects.size()));
            context.getPlayer().removeEffect(removed);
        }

        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.POST_DAMAGE, PLAYER_DAMAGE, this::onDamagePost);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        SkillHandler skills = context.getSkillHandler();
        Player player = context.getPlayer();
        double maxMana = player.getAttribute(AttributesInit.MAX_MANA).getValue();
        if (player.getEffect(EffectInit.HEALING_TOUCH) == null) {
            endSpell();
            return;
        }

        if (ticks % 20 == 0) {
            float heal = 0.5f;
            if (skills.hasSkill(SkillInit.HEALING_STREAM.value()))
                heal += (float) maxMana * 0.02f;
            player.heal(heal);

            if (skills.hasSkill(SkillInit.ACCELERATED_GROWTH.value())) {
                player.getFoodData().eat((int) (maxMana * 0.02d), 1f); //TODO: food values
            }

            if (skills.hasSkill(SkillInit.TRANQUILITY_OF_WATER.value())) {
                context.getSpellHandler().awardMana(4 + (skills.getSpellLevel(getSpellType()) * 0.4f));
                context.getSpellHandler().sync();
            }

            if (skills.hasSkill(SkillInit.OVERGROWTH.value()) && overgrowthStacks < 5) overgrowthStacks++;

            AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
            if (blessingCooldown > 0) blessingCooldown--;
            if (blessingCooldown <= 400 && armor.hasModifier(ARMOR_MOD)) {
                armor.removeModifier(ARMOR_MOD);
            }
        }
    }

    private void onDamagePost(PlayerDamageEvent.Post spellEvent) {
        LivingDamageEvent.Post  event = spellEvent.getDamageEvent();
        SkillHandler skills = caster.getData(DataInit.SKILL_HANDLER);
        if (event.getSource().getEntity().is(caster)) {
            if (event.getEntity().hasEffect(EffectInit.POISON) && skills.hasSkill(SkillInit.CONVALESCENCE.value()))
                caster.heal(0.5f);
        }

        if (!event.getEntity().is(caster)) return;
        if (overgrowthStacks > 0) {
            caster.heal(2f);
            overgrowthStacks--;
        }
        if (skills.hasSkill(SkillInit.VILE_INFUSION.value())) {
            StatusHandler status = event.getSource().getEntity().getData(DataInit.STATUS_EFFECTS);
            status.increment(StatusHandler.Effect.POISON, 50);
        }
        if (skills.hasSkill(SkillInit.OAK_BLESSING.value()) && blessingCooldown <= 0) {
            blessingCooldown = 600;
            caster.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(
                    ARMOR_MOD,
                    1.15d,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    @Override
    public int getDuration(SkillHandler skillHandler) {
        return skillHandler.hasSkill(SkillInit.BLOOM.value()) ?
                super.getDuration(skillHandler) * 2 : super.getDuration(skillHandler);
    }

    @Override
    public int getManaCost(SkillHandler skillHandler) {
        return skillHandler.hasSkill(SkillInit.BLOOM.value()) ?
                super.getManaCost(skillHandler) / 2 : super.getManaCost(skillHandler);
    }
}
