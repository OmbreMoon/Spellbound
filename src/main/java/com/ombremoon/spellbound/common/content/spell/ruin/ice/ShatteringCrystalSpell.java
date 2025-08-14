package com.ombremoon.spellbound.common.content.spell.ruin.ice;

import com.ombremoon.spellbound.common.content.entity.spell.ShatteringCrystal;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.RitualHelper;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.ModifierData;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ShatteringCrystalSpell extends AnimatedSpell {
    protected static final SpellDataKey<Integer> CRYSTAL = SyncedSpellData.registerDataKey(ShatteringCrystalSpell.class, SBDataTypes.INT.get());
    private static final ResourceLocation FRIGID_BLAST = CommonClass.customLocation("frigid_blast");
    private static final ResourceLocation HYPOTHERMIA = CommonClass.customLocation("hypothermia");

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

    private boolean primed;
    private int primeTick = 10;
    private int primeCount = 0;
    private boolean primedExternally;

    public ShatteringCrystalSpell() {
        super(SBSpells.SHATTERING_CRYSTAL.get(), createShatteringCrystalBuild());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        super.defineSpellData(builder);
        builder.define(CRYSTAL, 0);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        Level level = context.getLevel();
        LivingEntity caster = context.getCaster();
        var skills = context.getSkills();
        if (!level.isClientSide) {
            if (context.getTarget() instanceof ShatteringCrystal crystal && crystal.getOwner().is(caster)) {
                this.primeCrystal(context, crystal, true);
            } else {
                ShatteringCrystal crystal = this.summonEntity(context, SBEntities.SHATTERING_CRYSTAL.get());
                this.setCrystal(crystal.getId());
            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Level level = context.getLevel();
        LivingEntity caster = context.getCaster();
        var skills = context.getSkills();
        ShatteringCrystal crystal = this.getCrystal(context);
        if (crystal != null && (skills.hasSkill(SBSkills.THIN_ICE) || skills.hasSkill(SBSkills.CHILL))) {
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, crystal.getBoundingBox().inflate(4))
                    .stream()
                    .filter(livingEntity -> !this.isCaster(livingEntity) || SpellUtil.IS_ALLIED.test(caster, livingEntity))
                    .toList();

            if (skills.hasSkill(SBSkills.CHILL) && this.ticks % 20 == 0) {
                for (LivingEntity entity : entities) {
                    this.hurt(entity, this.getBaseDamage() / 2);
                }
            }

            if (skills.hasSkill(SBSkills.THIN_ICE) && !entities.isEmpty() && !this.primedExternally)
                this.primeCrystal(context, crystal, false);
        }

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

    private void primeCrystal(SpellContext context, ShatteringCrystal crystal, boolean endSpell) {
        var skills = context.getSkills();
        ShatteringCrystalSpell spell = crystal.getSpell();
        int count = skills.hasSkill(SBSkills.CRYSTAL_ECHO) ? 2 : 1;
        if (spell != null && spell.primeCount < count) {
            spell.primed = true;
            spell.primeTick = 10;
            spell.primeCount++;
        }

        if (endSpell)
            endSpell();
    }

    private void explodeCrystal(SpellContext context) {
        Level level = context.getLevel();
        var skills = context.getSkills();
        ShatteringCrystal crystal = this.getCrystal(context);
        if (crystal != null) {
            List<Entity> entities = level.getEntities(crystal, crystal.getBoundingBox().inflate(4));
            boolean flag = skills.hasSkill(SBSkills.CRYSTAL_ECHO);
            for (Entity entity : entities) {
                if (skills.hasSkill(SBSkills.CHAOTIC_SHATTER) && entity instanceof ShatteringCrystal crystal1) {
                    ShatteringCrystalSpell spell = crystal1.getSpell();
                    int count = flag ? 2 : 1;
                    if (!spell.primedExternally && spell.primeCount < count) {
                        this.primeCrystal(context, crystal, false);
                        spell.primedExternally = true;
                    }
                } else if (entity instanceof LivingEntity livingEntity && !this.isCaster(livingEntity) && this.hurt(livingEntity)) {
                    if (skills.hasSkill(SBSkills.FRIGID_BLAST))
                        this.addSkillBuff(
                                livingEntity,
                                SBSkills.FRIGID_BLAST,
                                BuffCategory.HARMFUL,
                                SkillBuff.ATTRIBUTE_MODIFIER,
                                new ModifierData(Attributes.MOVEMENT_SPEED, new AttributeModifier(HYPOTHERMIA, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                                100
                        );

                    if (skills.hasSkill(SBSkills.HYPOTHERMIA))
                        this.addSkillBuff(
                                livingEntity,
                                SBSkills.HYPOTHERMIA,
                                BuffCategory.HARMFUL,
                                SkillBuff.ATTRIBUTE_MODIFIER,
                                new ModifierData(SBAttributes.FROST_SPELL_RESIST, new AttributeModifier(HYPOTHERMIA, -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                                200
                        );
                }
            }

            if (skills.hasSkillReady(SBSkills.ICE_SHARD)) {
                RitualHelper.createItem(level, crystal.position(), new ItemStack(SBItems.FROZEN_SHARD.get()));
                this.addCooldown(SBSkills.ICE_SHARD, 24000);
            }

            if (flag) {
                this.setRemainingTicks(200);
            } else if (!level.isClientSide) {
                this.endSpell();
            }
        }

        this.primed = false;
        this.primeTick = 10;
    }

    private void setCrystal(int crystal) {
        this.spellData.set(CRYSTAL, crystal);
    }

    private ShatteringCrystal getCrystal(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.spellData.get(CRYSTAL));
        return entity instanceof ShatteringCrystal crystal ? crystal : null;
    }
}
