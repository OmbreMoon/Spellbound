package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.content.entity.spell.ShadowGate;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.*;
import com.ombremoon.spellbound.common.magic.api.buff.*;
import com.ombremoon.spellbound.util.SpellUtil;
import com.ombremoon.spellbound.util.portal.PortalInfo;
import com.ombremoon.spellbound.util.portal.PortalMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

//TODO: ADD OPEN/CLOSE ANIMATIONS

public class ShadowGateSpell extends AnimatedSpell implements RadialSpell {
    private static Builder<ShadowGateSpell> createShadowGateBuilder() {
        return createSimpleSpellBuilder(ShadowGateSpell.class)
                .mastery(SpellMastery.ADEPT)
                .manaCost(25)
                .duration(1200)
                .castCondition((context, spell) -> {
                    var skills = context.getSkills();
                    int activePortals = spell.portalMap.size();
                    boolean hasReach = skills.hasSkill(SBSkills.REACH);
                    BlockPos blockPos = spell.getSpawnPos(hasReach ? 100 : 50);
                    if (blockPos == null) return false;

                    if (!context.getLevel().getBlockState(blockPos).isAir()) return false;
                    if (activePortals > 1) {
                        int portalRange = hasReach ? 10000 : 2500;
                        PortalInfo info = spell.portalMap.get(spell.portalMap.getPreviousPortal());
                        double distance = info.position().distanceToSqr(blockPos.getCenter());
                        if (distance > portalRange) return false;
                    }

                    if (skills.hasSkill(SBSkills.DARKNESS_PREVAILS)) return true;
                    int i = context.getLevel().getRawBrightness(blockPos, 0) + context.getLevel().getBrightness(LightLayer.BLOCK, blockPos) - context.getLevel().getSkyDarken();
                    return i <= 4;
                })
                .summonCast()
                .fullRecast()
                .skipEndOnRecast();
    }
    private static final ResourceLocation UNWANTED_GUESTS = CommonClass.customLocation("unwanted_guests");

    private final PortalMap<ShadowGate> portalMap = new PortalMap<>();

    public ShadowGateSpell() {
        super(SBSpells.SHADOW_GATE.get(), createShadowGateBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        Level level = context.getLevel();
        var skills = context.getSkills();
        if (!level.isClientSide) {
            boolean hasReach = context.getSkills().hasSkill(SBSkills.REACH);
            this.summonEntity(context, SBEntities.SHADOW_GATE.get(), hasReach ? 100 : 50, shadowGate -> {
                int maxPortals = skills.hasSkill(SBSkills.DUAL_DESTINATION) ? 3 : 2;
                if (context.getFlag() == 1)
                    shadowGate.shift();

                this.portalMap.createOrShiftPortal(shadowGate, maxPortals, 20);
            });
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            var skills = context.getSkills();
            if (!this.portalMap.isEmpty()) {
                for (var entry : this.portalMap.entrySet()) {
                    ShadowGate shadowGate = (ShadowGate) level.getEntity(entry.getKey());
                    if (shadowGate != null) {
                        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, shadowGate.getBoundingBox());
                        List<LivingEntity> teleportList = new ObjectArrayList<>();
                        for (LivingEntity entity : entities) {
                            if (skills.hasSkill(SBSkills.OPEN_INVITATION)) {
                                teleportList.add(entity);
                            } else if (isCaster(entity)) {
                                teleportList.add(entity);
                            }
                        }

                        for (LivingEntity entity : teleportList) {
                            if (this.portalMap.attemptTeleport(entity, shadowGate)) {
                                if (skills.hasSkill(SBSkills.BLINK) && isCaster(entity))
                                    addSkillBuff(
                                            caster,
                                            SBSkills.BLINK,
                                            BuffCategory.BENEFICIAL,
                                            SkillBuff.ATTRIBUTE_MODIFIER,
                                            new ModifierData(Attributes.MOVEMENT_SPEED, new AttributeModifier(CommonClass.customLocation("blink"), 0.25F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                                            100);

                                if (skills.hasSkillReady(SBSkills.QUICK_RECHARGE)) {
                                    context.getSpellHandler().awardMana(10);
                                    addCooldown(SBSkills.QUICK_RECHARGE, 200);
                                }

                                if (skills.hasSkill(SBSkills.SHADOW_ESCAPE) && isCaster(entity) && caster.getHealth() < caster.getMaxHealth() * 0.5F && !caster.hasEffect(MobEffects.INVISIBILITY))
                                    caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, true));

                                if (!isCaster(entity) && skills.hasSkill(SBSkills.UNWANTED_GUESTS) && !entity.isAlliedTo(context.getCaster())) {
                                    addEventBuff(
                                            entity,
                                            SBSkills.UNWANTED_GUESTS,
                                            BuffCategory.HARMFUL,
                                            SpellEventListener.Events.PRE_DAMAGE,
                                            UNWANTED_GUESTS,
                                            pre -> pre.setNewDamage(pre.getOriginalDamage() * 0.9F),
                                            200);
                                    addSkillBuff(
                                            entity,
                                            SBSkills.UNWANTED_GUESTS,
                                            BuffCategory.HARMFUL,
                                            SkillBuff.SPELL_MODIFIER,
                                            SpellModifier.UNWANTED_GUESTS,
                                            200);
                                }

                                if (skills.hasSkill(SBSkills.BAIT_AND_SWITCH) && !entity.isAlliedTo(caster)) {
                                    this.hurt(entity, 5);
                                    SpellUtil.getSpellCaster(entity).consumeMana(5);
                                }

                                ShadowGate adjacentGate = this.portalMap.getAdjacentPortal(shadowGate, level);
                                if (adjacentGate.isShifted()) {
                                    Vec3 lookVec = adjacentGate.getViewVector(1.0F);
                                    entity.setDeltaMovement(lookVec.x, 2, lookVec.z);
                                    entity.hurtMarked = true;
                                    if (isCaster(entity))
                                        entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            this.portalMap.forEach((id, portalInfo) -> {
                Entity entity = context.getLevel().getEntity(id);
                if (entity instanceof ShadowGate shadowGate)
                    shadowGate.setEndTick(20);
            });
        }
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        this.portalMap.serialize(compoundTag);
        return compoundTag;
    }

    @Override
    public void loadData(CompoundTag nbt) {
        this.portalMap.deserialize(nbt);
    }
}
