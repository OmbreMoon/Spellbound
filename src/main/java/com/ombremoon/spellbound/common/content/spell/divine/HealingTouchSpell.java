package com.ombremoon.spellbound.common.content.spell.divine;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.magic.EffectManager;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.buff.events.DamageEvent;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.UnknownNullability;

public class HealingTouchSpell extends AnimatedSpell {
    private static final ResourceLocation ARMOR_MOD = CommonClass.customLocation("oak_blessing_mod");
    private static final ResourceLocation PLAYER_DAMAGE = CommonClass.customLocation("healing_touch_player_damage");

    private LivingEntity caster;
    private int overgrowthStacks = 0;
    private int blessingDuration = 0;

    private static Builder<HealingTouchSpell> createHealingSpell() {
        return createSimpleSpellBuilder(HealingTouchSpell.class)
                .manaCost(15)
                .duration(100)
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

        SkillHolder skills = context.getSkills();
        this.caster = context.getCaster();
        if (skills.hasSkill(SBSkills.NATURES_TOUCH.value())) context.getCaster().heal(4f);

        if (skills.hasSkill(SBSkills.CLEANSING_TOUCH.value())) this.cleanseCaster(1);

    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        if (context.getLevel().isClientSide) return;
        SkillHolder skills = context.getSkills();
        LivingEntity caster = context.getCaster();
        double maxMana = caster.getAttribute(SBAttributes.MAX_MANA).getValue();

        float heal = 2;
        if (skills.hasSkill(SBSkills.HEALING_STREAM.value()))
            heal += (float) maxMana * 0.02f;

        this.heal(caster, heal);

        if (skills.hasSkill(SBSkills.ACCELERATED_GROWTH.value()) && caster instanceof Player player) {
            player.getFoodData().eat((int) (maxMana * 0.02d), 1f);
        }

        if (skills.hasSkill(SBSkills.TRANQUILITY_OF_WATER.value()))
            context.getSpellHandler().awardMana(2);

        if (skills.hasSkill(SBSkills.OVERGROWTH.value()) && overgrowthStacks < 5) overgrowthStacks++;

        AttributeInstance armor = caster.getAttribute(Attributes.ARMOR);
        if (blessingDuration > 0) blessingDuration--;
        if (blessingDuration <= 0 && armor.hasModifier(ARMOR_MOD)) {
            armor.removeModifier(ARMOR_MOD);
        }

        for (int j = 0; j < 5; j++) {
            this.createSurroundingServerParticles(caster, SBParticles.GOLD_HEART.get(), 1);
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
    }

    @Override
    protected boolean shouldTickSpellEffect(SpellContext context) {
        return tickCount % 20 == 0;
    }

    private void onDamagePost(DamageEvent.Post event) {
        SkillHolder skills = SpellUtil.getSkills(caster);

        if (event.getEntity().hasEffect(SBEffects.POISON) && skills.hasSkill(SBSkills.CONVALESCENCE.value()))
            caster.heal(1);

        if (!event.getEntity().is(caster)) return;
        if (overgrowthStacks > 0) {
            caster.heal(4f);
            overgrowthStacks--;
        }
        if (skills.hasSkillReady(SBSkills.BLASPHEMY.value())) {
            EffectManager status = event.getEntity().getData(SBData.STATUS_EFFECTS);
//            status.increment(EffectManager.Effect.DISEASE, 100);
            addCooldown(SBSkills.BLASPHEMY, 100);
        }
        if (skills.hasSkillReady(SBSkills.OAK_BLESSING.value())) {
            blessingDuration = 200;
            caster.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(
                    ARMOR_MOD,
                    1.15d,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
            addCooldown(SBSkills.OAK_BLESSING, 600);
        }
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        compoundTag.putInt("overgrowth", this.overgrowthStacks);
        compoundTag.putInt("blessing", this.blessingDuration);
        return compoundTag;
    }

    @Override
    public void loadData(CompoundTag nbt) {
        this.overgrowthStacks = nbt.getInt("overgrowth");
        this.blessingDuration = nbt.getInt("blessing");
    }

}
