package com.ombremoon.spellbound.common.content.spell.deception;

import com.ombremoon.spellbound.common.content.world.effect.SBEffectInstance;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.ModifierData;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.main.CommonClass;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

public class ShadowbondSpell extends AnimatedSpell {
    private static final ResourceLocation SNEAK_ATTACK = CommonClass.customLocation("sneak_attack");
    private static final ResourceLocation DISORIENTED = CommonClass.customLocation("disoriented");
    private static final SpellDataKey<Boolean> EARLY_END = SyncedSpellData.registerDataKey(ShadowbondSpell.class, SBDataTypes.BOOLEAN.get());
    public static Builder<ShadowbondSpell> createShadowbondBuilder() {
        return createSimpleSpellBuilder(ShadowbondSpell.class)
                .mastery(SpellMastery.ADEPT)
                .duration(300)
                .manaCost(35)
                .castCondition((context, spell) -> {
                    if (context.isRecast()) {
                        if (spell.canReverse) {
                            return true;
                        } else if (context.getSkills().hasSkill(SBSkills.SHADOW_CHAIN.value()) && context.getTarget() != null && context.getTarget().getId() != spell.firstTarget) {
                            return spell.secondTarget == 0;
                        } else if (!spell.isEarlyEnd() && !spell.canReverse) {
                            spell.spellData.set(EARLY_END, true);
                            return true;
                        }
                    }
                    return !context.isRecast() && context.getTarget() instanceof LivingEntity target && !spell.checkForCounterMagic(target);
                })
                .fullRecast()
                .skipEndOnRecast()
                .castAnimation(context -> "final01");
    }

    private int firstTarget;
    private int secondTarget;
    private boolean canReverse = false;
    private IntList targetList = new IntArrayList();

    public ShadowbondSpell() {
        super(SBSpells.SHADOWBOND.get(), createShadowbondBuilder());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        super.defineSpellData(builder);
        builder.define(EARLY_END, false);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        Level level = context.getLevel();
        Entity target = context.getTarget();
        LivingEntity caster = context.getCaster();
        var skills = context.getSkills();
        if (target == null)
            return;

        int id = target.getId();
        boolean flag = skills.hasSkill(SBSkills.SHADOW_CHAIN.value());

        if (id > 0) {
            if (context.isRecast() && !this.canReverse && flag && this.secondTarget == 0) {
                this.secondTarget = id;
                this.targetList.add(id);
            } else if (!context.isRecast() && this.firstTarget == 0){
                this.firstTarget = id;
                this.targetList.add(id);
            }
        }

        if (!this.canReverse) {
            MobEffectInstance mobEffectInstance = new SBEffectInstance(caster, MobEffects.INVISIBILITY, -1, skills.hasSkill(SBSkills.OBSERVANT.value()), 0, false, false);
            addSkillBuff(
                    caster,
                    SBSkills.SHADOWBOND.value(),
                    BuffCategory.BENEFICIAL,
                    SkillBuff.MOB_EFFECT,
                    mobEffectInstance);

            int size = this.targetList.size();
            for (int i = 0; i < size + 1; i++) {
                Entity entity = i < size ? level.getEntity(this.targetList.get(i)) : caster;
                if (entity instanceof LivingEntity living) {
                    addSkillBuff(
                            living,
                            SBSkills.SHADOWBOND.value(),
                            BuffCategory.HARMFUL,
                            SkillBuff.MOB_EFFECT,
                            mobEffectInstance);

                    this.spawnTeleportParticles(entity, 40);
                }
            }

            level.playSound(null, caster.xo, caster.yo, caster.zo, SoundEvents.ENDERMAN_TELEPORT, caster.getSoundSource(), 1.0F, 1.0F);
            caster.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
    }

    @Override
    protected void onSpellRecast(SpellContext context) {
        super.onSpellRecast(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        var skills = context.getSkills();
        if (this.canReverse || this.isEarlyEnd()) {
            swapTargets(context, caster, level, skills);

            if (this.isEarlyEnd()) {
                this.setRemainingTicks(100);
                this.canReverse = skills.hasSkill(SBSkills.REVERSAL.value()) && !this.targetList.isEmpty();
                if (!this.canReverse)
                    endSpell();
            } else {
                endSpell();
            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        var skills = context.getSkills();
        for (Integer entityId : this.targetList) {
            Entity entity = level.getEntity(entityId);
            if (entity == null || (entity instanceof LivingEntity living && checkForCounterMagic(living)))
                this.targetList.remove(entityId);
        }

        int remainder = this.getRemainingTime();
        if (remainder == 100) {
            swapTargets(context, caster, level, skills);
        } else if (remainder < 100) {
            this.canReverse = skills.hasSkill(SBSkills.REVERSAL.value()) && !this.targetList.isEmpty();
        }

        if (this.targetList.isEmpty())
            endSpell();
    }

    @Override
    protected void onSpellStop(SpellContext context) {

    }

    @Override
    protected int getDuration(SpellContext context) {
        var skills = context.getSkills();
        return skills.hasSkill(SBSkills.EVERLASTING_BOND.value()) ? 500 :  super.getDuration(context);
    }

    private void teleport(LivingEntity first, LivingEntity second) {
        Vec3 firstPos = first.position();
        Vec3 secondPosPos = second.position();
        first.teleportTo(secondPosPos.x, secondPosPos.y, secondPosPos.z);
        second.teleportTo(firstPos.x, firstPos.y, firstPos.z);

        this.spawnTeleportParticles(first, 40);
        this.spawnTeleportParticles(second, 40);
    }

    private void chainTeleport(LivingEntity caster, LivingEntity first, LivingEntity second) {
        Vec3 playerPos = caster.position();
        Vec3 firstPos = first.position();
        Vec3 secondPos = second.position();
        caster.teleportTo(secondPos.x, secondPos.y, secondPos.z);
        second.teleportTo(firstPos.x, firstPos.y, firstPos.z);
        first.teleportTo(playerPos.x, playerPos.y, playerPos.z);

        this.spawnTeleportParticles(caster, 40);
        this.spawnTeleportParticles(first, 40);
        this.spawnTeleportParticles(second, 40);
    }

    private void swapTargets(SpellContext context, LivingEntity caster, Level level, SkillHolder skills) {
        Entity entity = level.getEntity(this.firstTarget);
        Entity secondEntity = level.getEntity(this.secondTarget);
        boolean flag = true;
        if (entity instanceof LivingEntity living && secondEntity instanceof LivingEntity secondLiving) {
            chainTeleport(caster, living, secondLiving);
        } else if (entity instanceof LivingEntity living) {
            teleport(caster, living);
        } else if (secondEntity instanceof LivingEntity secondLiving) {
            teleport(caster, secondLiving);
        } else {
            flag = false;
        }

        if (flag)
            handleSwapEffect(context, caster, level, skills);

        level.playSound(null, caster.xo, caster.yo, caster.zo, SoundEvents.ENDERMAN_TELEPORT, caster.getSoundSource(), 1.0F, 1.0F);
        caster.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    private void handleSwapEffect(SpellContext context, LivingEntity caster, Level level, SkillHolder skills) {
        if (skills.hasSkill(SBSkills.SHADOW_STEP.value()))
            addSkillBuff(
                    caster,
                    SBSkills.SHADOW_STEP.value(),
                    BuffCategory.BENEFICIAL,
                    SkillBuff.ATTRIBUTE_MODIFIER,
                    new ModifierData(Attributes.MOVEMENT_SPEED, new AttributeModifier(CommonClass.customLocation("shadow_step"), 1.3F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                    100);

        if (skills.hasSkill(SBSkills.SNEAK_ATTACK.value())) {
            addSkillBuff(
                    caster,
                    SBSkills.SNEAK_ATTACK.value(),
                    BuffCategory.BENEFICIAL,
                    SkillBuff.ATTRIBUTE_MODIFIER,
                    new ModifierData(Attributes.ATTACK_DAMAGE, new AttributeModifier(SNEAK_ATTACK, 1.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                    100);
            addEventBuff(
                    caster,
                    SBSkills.SNEAK_ATTACK.value(),
                    BuffCategory.BENEFICIAL,
                    SpellEventListener.Events.ATTACK,
                    SNEAK_ATTACK,
                    pre -> removeSkillBuff(caster, SBSkills.SNEAK_ATTACK.value()),
                    100);
        }

        removeSkillBuff(caster, SBSkills.SHADOWBOND.value());
        for (Integer entityId : this.targetList) {
            Entity effectEntity = level.getEntity(entityId);
            if (effectEntity instanceof LivingEntity living) {
                removeSkillBuff(living, SBSkills.SHADOWBOND.value());
                if (skills.hasSkill(SBSkills.SILENT_EXCHANGE.value()))
                    living.addEffect(new MobEffectInstance(SBEffects.SILENCED, 100, 0, false, true));

                if (skills.hasSkill(SBSkills.SNARE.value()))
                    living.addEffect(new MobEffectInstance(SBEffects.ROOTED, 100, 0, false, true));

                if (skills.hasSkill(SBSkills.DISORIENTED.value())) {
                    living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
                    addEventBuff(
                            living,
                            SBSkills.DISORIENTED.value(),
                            BuffCategory.HARMFUL,
                            SpellEventListener.Events.PRE_DAMAGE,
                            DISORIENTED,
                            pre -> pre.setNewDamage(pre.getOriginalDamage() * 0.8F),
                            100);

                }
//                context.getSpellHandler().applyFear(living, 100);
            }
        }

        if (skills.hasSkill(SBSkills.LIVING_SHADOW.value())) {
            summonEntity(context, SBEntities.LIVING_SHADOW.get(), caster.position(), livingShadow -> {});
            caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false));
        }
    }

    private void spawnTeleportParticles(Entity entity ,int amount) {
        for (int j = 0; j < amount; j++) {
            this.createSurroundingParticles(entity, ParticleTypes.PORTAL, 0.5);
        }
    }

    private boolean isEarlyEnd() {
        return this.spellData.get(EARLY_END);
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        compoundTag.putInt("FirstTargetId", this.firstTarget);
        compoundTag.putInt("SecondTargetId", this.secondTarget);
        compoundTag.putIntArray("TargetList", this.targetList);
        compoundTag.putBoolean("CanReverse", this.canReverse);
        return compoundTag;
    }

    @Override
    public void loadData(CompoundTag nbt) {
        super.loadData(nbt);
        this.firstTarget = nbt.getInt("FirstTargetId");
        this.secondTarget = nbt.getInt("SecondTargetId");
        this.targetList = IntArrayList.of(nbt.getIntArray("TargetList"));
        this.canReverse = nbt.getBoolean("CanReverse");
    }
}