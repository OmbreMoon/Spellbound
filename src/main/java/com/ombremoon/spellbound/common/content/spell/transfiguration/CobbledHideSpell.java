package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class CobbledHideSpell extends AnimatedSpell {
    private static final ResourceLocation hideMod = CommonClass.customLocation("hide_spell_mod");
    private static final UUID PRE_DAMAGE = UUID.fromString("07e4170f-6d15-4ae6-9531-b727cfe39f96");

    private List<LivingEntity> buffedTargets;

    private static Builder<CobbledHideSpell> hideSpellBuilder() {
        return createSimpleSpellBuilder(CobbledHideSpell.class).manaCost(30).castTime(20).duration(1200);
    }

    public CobbledHideSpell(SpellType<?> spellType) {
        super(spellType, hideSpellBuilder());
    }


    //Damage based skills needed to be added
    @Override
    protected void onSpellStart(SpellContext context) {
        if (context.isRecast()) return;
        SkillHandler skills = context.getSkills();

        float modAmount = 1.1f;
        if (skills.hasSkill(SkillInit.DRAGON_HIDE.value())) modAmount = 1.5f;
        else if (skills.hasSkill(SkillInit.DIAMOND_HIDE.value())) modAmount = 1.3f;
        else if (skills.hasSkill(SkillInit.IRON_HIDE.value())) modAmount = 1.2f;

        context.getSpellHandler().addTransientModifier(Attributes.ARMOR,
                new AttributeModifier(hideMod, modAmount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), getDuration());

        if (skills.hasSkill(SkillInit.INFECTIOUS.value())) {
            Predicate<LivingEntity> predicate;
            if (skills.hasSkill(SkillInit.VIRAL.value()))
                predicate = entity -> entity instanceof Player || (entity instanceof OwnableEntity ownable && ownable.getOwner().is(context.getPlayer()));
            else predicate = entity -> entity instanceof OwnableEntity ownable && ownable.getOwner().is(context.getPlayer());

            this.buffedTargets = context.getLevel().getEntitiesOfClass(LivingEntity.class,
                    context.getPlayer().getBoundingBox().inflate(8d), predicate);

            double allyBuff = skills.hasSkill(SkillInit.REINFORCED.value()) ? modAmount : modAmount/2d;
            buffedTargets.forEach(mob -> mob.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(
                    hideMod, allyBuff, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )));
        }
    }

    @Override
    protected boolean shouldTickEffect(SpellContext context) {
        return false;
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        if (buffedTargets != null) {
            buffedTargets.forEach(mob -> mob.getAttribute(Attributes.ARMOR).removeModifier(hideMod));
        }

        context.getPlayer().getAttribute(Attributes.ARMOR).removeModifier(hideMod);
    }

    @Override
    protected void onSpellRecast(SpellContext context) {
        if (context.getSkills().hasSkill(SkillInit.REPULSIVE_SKIN.value())) {
            Player player = context.getPlayer();
            List<LivingEntity> entities = context.getLevel().getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(8d));

            for (LivingEntity entity : entities) {
                entity.hurt(context.getPlayer().damageSources().magic(), 2f);
                entity.knockback(1d,
                        entity.getX() - player.getX(),
                        entity.getZ() - player.getZ());
            }

            endSpell();
        }
    }
}
