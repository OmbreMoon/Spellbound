package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

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
    private BlockPos currentPos;
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
        if (!level.isClientSide) {
            applyMovementBenefits(caster, level, skills);

            if (caster instanceof Player player) {
                if (skills.hasSkill(SBSkills.MARATHON.value()))
                    this.initialFoodLevel = player.getFoodData().getFoodLevel();

                if (skills.hasSkill(SBSkills.MOMENTUM.value()))
                    this.currentPos = context.getBlockPos();
            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        var skills = context.getSkills();

        if (skills.hasSkill(SBSkills.AQUA_TREAD.value())) {
            Entity entity = caster.getVehicle() != null && skills.hasSkill(SBSkills.RIDERS_RESILIENCE.value()) ? caster.getVehicle() : caster;
            entity.wasTouchingWater = false;
            Vec3 vec3 = entity.getDeltaMovement().scale(1.15F);
            if (entity.getBlockStateOn().is(Blocks.WATER)) {
                entity.setDeltaMovement(vec3.x, 0.025, vec3.z);
                entity.setSwimming(false);
                entity.setOnGround(true);
                float f1 = Math.min(1.0F, (float)Math.sqrt(vec3.x * vec3.x * 0.2F + vec3.y * vec3.y + vec3.z * vec3.z * 0.2F) * 0.35F);
                caster.playSound(SoundEvents.PLAYER_SWIM, f1, 1.0F + (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.4F);
            }
        }

        if (!level.isClientSide) {
            if (skills.hasSkill(SBSkills.QUICK_SPRINT.value()) && this.ticks >= 200) {
                if (hasAttributeModifier(caster, Attributes.MOVEMENT_SPEED, QUICK_SPRINT)) {
                    removeAttributeModifier(caster, Attributes.MOVEMENT_SPEED, QUICK_SPRINT);
                } else if (caster.getVehicle() instanceof LivingEntity vehicle && hasAttributeModifier(vehicle, Attributes.MOVEMENT_SPEED, QUICK_SPRINT)) {
                    removeAttributeModifier(vehicle, Attributes.MOVEMENT_SPEED, QUICK_SPRINT);
                }
            }

            if (skills.hasSkill(SBSkills.FLEETFOOTED.value())) {
                var allies = level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(5), livingEntity -> livingEntity.isAlliedTo(caster));
                for (LivingEntity ally : allies) {
                    addTimedAttributeModifier(ally, Attributes.MOVEMENT_SPEED, new AttributeModifier(FLEETFOOTED, 1.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), 20);
                }
            }

            if (skills.hasSkill(SBSkills.RIDERS_RESILIENCE.value())) {
                Entity entity = caster.getVehicle();
                if (entity instanceof LivingEntity livingEntity) {
                    this.mount = livingEntity;
                    applyMovementBenefits(livingEntity, level, skills);
                } else if (this.mount != null) {
                    removeMovementBenefits(this.mount);
                    this.mount = null;
                }
            }

            if (skills.hasSkill(SBSkills.STAMPEDE.value())) {
                var entities = level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(1.5), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
                boolean flag = caster.getVehicle() != null && skills.hasSkill(SBSkills.RIDERS_RESILIENCE.value());
                for (LivingEntity living : entities) {
                    if (!isCaster(living) && !living.isAlliedTo(caster) && (caster.isSprinting() || flag && !living.is(caster.getVehicle()))) {
                        living.knockback(0.4, caster.getX() - living.getX(), caster.getZ() - living.getZ());
                        living.hurtMarked = true;
                        living.hurt(level.damageSources().mobAttack(caster), 2.5F);
                    }
                }
            }

            if (skills.hasSkill(SBSkills.MOMENTUM.value())) {
                if (this.ticks % 4 == 0) {
                    log(this.movementTicks);
                    if (!this.currentPos.equals(caster.getOnPos())) {
                        this.movementTicks += 4;
                        this.currentPos = caster.getOnPos();
                        if (this.movementTicks % 20 == 0)
                            addTimedAttributeModifier(caster, Attributes.ATTACK_SPEED, new AttributeModifier(MOMENTUM, 1 + (0.2 * this.movementTicks / 20), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), 100);
                    } else {
                        this.movementTicks = 0;
                    }
                }
            }

            if (skills.hasSkill(SBSkills.MARATHON.value()) && caster instanceof Player player && player.getFoodData().getFoodLevel() < this.initialFoodLevel)
                player.getFoodData().eat(this.initialFoodLevel - player.getFoodData().getFoodLevel(), 0);
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

    private void applyMovementBenefits(LivingEntity livingEntity, Level level, SkillHolder skills) {
        addAttributeModifier(livingEntity, Attributes.MOVEMENT_SPEED, new AttributeModifier(THUNDEROUS_HOOVES, skills.hasSkill(SBSkills.GALLOPING_STRIDE.value()) ? 1.5 : 1.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (skills.hasSkill(SBSkills.QUICK_SPRINT.value()))
            addAttributeModifier(livingEntity, Attributes.MOVEMENT_SPEED, new AttributeModifier(QUICK_SPRINT, 1.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        if (skills.hasSkill(SBSkills.SUREFOOTED.value()))
            addAttributeModifier(livingEntity, Attributes.STEP_HEIGHT, new AttributeModifier(SUREFOOTED, 0.4, AttributeModifier.Operation.ADD_VALUE));
    }
}