package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ThunderousHoovesSpell extends AnimatedSpell {
    protected static final ResourceLocation THUNDEROUS_HOOVES = CommonClass.customLocation("thunderous_hooves");
    protected static final ResourceLocation QUICK_SPRINT = CommonClass.customLocation("quick_sprint");
    protected static final ResourceLocation SUREFOOTED = CommonClass.customLocation("surefooted");
    protected static final ResourceLocation FLEETFOOTED = CommonClass.customLocation("fleetfooted");
    protected static final ResourceLocation MOMENTUM = CommonClass.customLocation("momentum");

    public static Builder<ThunderousHoovesSpell> createThunderousHoovesBuilder() {
        return createSimpleSpellBuilder(ThunderousHoovesSpell.class).duration(600).fullRecast();
    }

    private int initialFoodLevel;
    private int movementTicks;
    private LivingEntity mount;

    public ThunderousHoovesSpell() {
        super(SBSpells.THUNDEROUS_HOOVES.get(), createThunderousHoovesBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        var skills = context.getSkills();
        if (!level.isClientSide)
            applyMovementBenefits(caster, skills);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        var skills = context.getSkills();
        if (!level.isClientSide) {
            if (skills.hasSkill(SBSkills.QUICK_SPRINT.value()) && this.ticks >= 200) {
                if (hasAttributeModifier(caster, Attributes.MOVEMENT_SPEED, QUICK_SPRINT)) {
                    removeAttributeModifier(caster, Attributes.MOVEMENT_SPEED, QUICK_SPRINT);
                } else if (caster.getVehicle() instanceof LivingEntity vehicle && hasAttributeModifier(vehicle, Attributes.MOVEMENT_SPEED, QUICK_SPRINT)) {
                    removeAttributeModifier(vehicle, Attributes.MOVEMENT_SPEED, QUICK_SPRINT);
                }
            }

            if (skills.hasSkill(SBSkills.FLEETFOOTED.value())) {
                var allies = level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(3), livingEntity -> livingEntity.isAlliedTo(caster));
                for (LivingEntity ally : allies) {
                    addTimedAttributeModifier(ally, Attributes.MOVEMENT_SPEED, new AttributeModifier(FLEETFOOTED, 1.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), 20);
                }
            }

            if (skills.hasSkill(SBSkills.RIDERS_RESILIENCE.value())) {
                Entity entity = caster.getVehicle();
                if (entity instanceof LivingEntity livingEntity) {
                    this.mount = livingEntity;
                    applyMovementBenefits(livingEntity, skills);
                } else if (this.mount != null) {
                    removeMovementBenefits(this.mount);
                    this.mount = null;
                }
            }

            Vec3 vec3 = caster.getDeltaMovement();
            if (caster.isInWater()) {
                caster.setDeltaMovement(vec3.x, 0.002, vec3.z);
                caster.hurtMarked = true;
                caster.setSwimming(false);
                caster.setOnGround(true);
            }
            log(caster.onGround());

            if (skills.hasSkill(SBSkills.MOMENTUM.value())) {
                if (caster.walkAnimation.isMoving()) {
                    this.movementTicks++;
                    if (this.movementTicks % 20 == 0)
                        addTimedAttributeModifier(caster, Attributes.ATTACK_SPEED, new AttributeModifier(MOMENTUM, 1 + (0.2 * this.movementTicks / 20), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), 100);
                }
            }

            if (skills.hasSkill(SBSkills.STAMPEDE.value())) {
                var entities = level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(2), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
                for (LivingEntity livingEntity : entities) {
                    if (!livingEntity.isAlliedTo(caster) && caster.isSprinting()) {
                        livingEntity.knockback(0.4, caster.getX() - livingEntity.getX(), caster.getZ() - livingEntity.getZ());
                        livingEntity.hurt(level.damageSources().mobAttack(caster), 2.5F);
                    }
                }
            }

            if (skills.hasSkill(SBSkills.MARATHON.value()) && caster instanceof Player player && player.getFoodData().getFoodLevel() < this.initialFoodLevel)
                player.getFoodData().eat(this.initialFoodLevel - player.getFoodData().getFoodLevel(), 0);

            log(ticks);
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        LivingEntity caster = context.getCaster();
        removeMovementBenefits(caster);
        if (this.mount != null)
            removeMovementBenefits(this.mount);
    }

    private void removeMovementBenefits(LivingEntity livingEntity) {
        if (hasAttributeModifier(livingEntity, Attributes.MOVEMENT_SPEED, THUNDEROUS_HOOVES))
            removeAttributeModifier(livingEntity, Attributes.MOVEMENT_SPEED, THUNDEROUS_HOOVES);

        if (hasAttributeModifier(livingEntity, Attributes.STEP_HEIGHT, SUREFOOTED))
            removeAttributeModifier(livingEntity, Attributes.STEP_HEIGHT, SUREFOOTED);
    }

    private void applyMovementBenefits(LivingEntity livingEntity, SkillHolder skills) {
        addAttributeModifier(livingEntity, Attributes.MOVEMENT_SPEED, new AttributeModifier(THUNDEROUS_HOOVES, skills.hasSkill(SBSkills.GALLOPING_STRIDE.value()) ? 1.5 : 1.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (skills.hasSkill(SBSkills.QUICK_SPRINT.value()))
            addAttributeModifier(livingEntity, Attributes.MOVEMENT_SPEED, new AttributeModifier(QUICK_SPRINT, 1.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        if (skills.hasSkill(SBSkills.SUREFOOTED.value()))
            addAttributeModifier(livingEntity, Attributes.STEP_HEIGHT, new AttributeModifier(SUREFOOTED, 0.4, AttributeModifier.Operation.ADD_VALUE));

        if (skills.hasSkill(SBSkills.MARATHON.value()) && livingEntity instanceof Player player)
            this.initialFoodLevel = player.getFoodData().getFoodLevel();
    }
}
