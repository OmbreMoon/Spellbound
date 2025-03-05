package com.ombremoon.spellbound.common.content.spell.ruin.shock;

import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.util.RandomUtil;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Set;

public class ElectricChargeSpell extends AnimatedSpell {
    private static final SpellDataKey<Integer> DISCHARGE_TICK = SyncedSpellData.registerDataKey(ElectricChargeSpell.class, SBDataTypes.INT.get());
    public static Builder<ElectricChargeSpell> createElectricChargeBuilder() {
        return createSimpleSpellBuilder(ElectricChargeSpell.class)
                .duration(200)
                .castType(CastType.CHARGING)
                .castCondition((context, spell) -> {
                    Entity entity = context.getTarget();
                    if (spell.discharging) return false;
                    if (entity != null) {
                        return !spell.entityIds.contains(entity.getId());
                    } else {
                        if (spell.entityIds.size() >= 3) spell.discharged = true;
                        return context.isRecast();
                    }
                }).fullRecast().updateInterval(1);
    }

    private Set<Integer> entityIds = new IntOpenHashSet();
    private boolean discharged;
    private boolean discharging;

    public ElectricChargeSpell() {
        super(SBSpells.ELECTRIC_CHARGE.get(), createElectricChargeBuilder());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        super.defineSpellData(builder);
        builder.define(DISCHARGE_TICK, 0);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        Entity target = context.getTarget();
        if (target != null && this.entityIds.size() < 3)
            this.entityIds.add(target.getId());
    }

    @Override
    protected void onSpellRecast(SpellContext context) {
        super.onSpellRecast(context);
        Level level = context.getLevel();
        var skills = context.getSkills();
        boolean hasShard = context.hasCatalyst(SBItems.STORM_SHARD.get());
        if (skills.hasSkill(SBSkills.AMPLIFY.value())) {
            context.getSpellHandler().setChargingOrChannelling(true);
            return;
        }
        if (context.getTarget() == null || this.discharged) {
            for (Integer entityId : this.entityIds) {
                Entity entity = level.getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity)
                    discharge(context, livingEntity, hasShard);
            }
            endSpell();
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {

    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Level level = context.getLevel();
        var handler = context.getSpellHandler();
        var skills = context.getSkills();
        boolean hasShard = context.hasCatalyst(SBItems.STORM_SHARD.get());
        if (skills.hasSkill(SBSkills.AMPLIFY.value())) {
            if ((context.isRecast() && context.getTarget() == null) || this.discharged) {
                this.discharging = true;
                if (handler.isChargingOrChannelling()) {
                    incrementTick();
                } else {
                    for (Integer entityId : this.entityIds) {
                        Entity entity = level.getEntity(entityId);
                        if (entity instanceof LivingEntity livingEntity)
                            discharge(context, livingEntity, hasShard);
                    }
                    endSpell();
                }
            }
        }
    }

    private void discharge(SpellContext context, LivingEntity target, boolean hasShard) {
        Level level = context.getLevel();
        LivingEntity caster = context.getCaster();
        var handler = context.getSpellHandler();
        var skills = context.getSkills();
        if (!level.isClientSide) {
            float damage = 10;
            if (skills.hasSkillReady(SBSkills.OSCILLATION)) {
                if (caster instanceof Player player) {
                    for (ItemStack itemStack : player.getInventory().items) {
                        if (itemStack.is(SBItems.STORM_SHARD.get())) {
                            for (int i = 0; i < Math.min(itemStack.getCount(), 10); i++) {
                                damage *= 1.05F;
                            }
                        }
                    }
                }
                addCooldown(SBSkills.OSCILLATION, 600);
            }
            float amplify = 1.0F + getPowerForTime(getDischargeTick(), 60);
            damage *= amplify;

            var entities = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            if (hurt(target, SBDamageTypes.RUIN_SHOCK, damage)) {
                if (target.isDeadOrDying()) {
                    if (skills.hasSkill(SBSkills.STORM_SURGE))
                        handler.awardMana(10 + (skills.getSpellLevel(getSpellType()) * 2));

                    if (skills.hasSkill(SBSkills.UNLEASHED_STORM.value())) {
                        for (LivingEntity targetEntity : entities) {
                            if (!isCaster(targetEntity))
                                hurt(targetEntity, SBDamageTypes.RUIN_SHOCK, damage / 2);
                        }
                    }

                    if (skills.hasSkill(SBSkills.STORM_CHARGE.value()) && caster instanceof Player player)
                        player.addItem(new ItemStack(SBItems.STORM_SHARD.get()));
                }
            }

            if (!checkForCounterMagic(target)) {
                if (skills.hasSkill(SBSkills.ELECTRIFICATION.value()))
                    handler.applyStormStrike(target, 60);

                if (skills.hasSkill(SBSkills.HIGH_VOLTAGE.value()) && hasShard) {
                    MobEffectInstance mobEffectInstance = new MobEffectInstance(SBEffects.STUNNED, 60, 0, false, false);
                    target.addEffect(mobEffectInstance);
                    for (LivingEntity paralysisTarget : entities) {
                        if (!isCaster(paralysisTarget))
                            paralysisTarget.addEffect(mobEffectInstance);
                    }
                    context.useCatalyst(SBItems.STORM_SHARD.get());
                    addCooldown(SBSkills.HIGH_VOLTAGE, 600);
                }

                if (skills.hasSkill(SBSkills.ALTERNATING_CURRENT.value())) {
                    if (RandomUtil.percentChance(potency(0.03F)) && target.getHealth() < caster.getHealth() * 2) {
                        target.kill();
                        if (skills.hasSkill(SBSkills.STORM_CHARGE) && caster instanceof Player player)
                            player.addItem(new ItemStack(SBItems.STORM_SHARD.get()));
                    } else {
                        hurt(caster, SBDamageTypes.RUIN_SHOCK, caster.getMaxHealth() * 0.05F);
                    }
                }

                if (skills.hasSkill(SBSkills.CHAIN_REACTION.value())) {
                    for (LivingEntity livingEntity : entities) {
                        if (!this.entityIds.contains(livingEntity.getId())) {
                            this.entityIds.add(livingEntity.getId());
                            discharge(context, target, hasShard);
                        }
                    }
                }
            }
        }
    }

    private int getDischargeTick() {
        return this.spellData.get(DISCHARGE_TICK);
    }

    private void incrementTick() {
        this.spellData.set(DISCHARGE_TICK, getDischargeTick() + 1);
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        compoundTag.putIntArray("ChargedTargets", this.entityIds.stream().toList());
        compoundTag.putBoolean("Discharged", this.discharged);
        compoundTag.putBoolean("Discharging", this.discharging);
        return compoundTag;
    }

    @Override
    public void loadData(CompoundTag nbt) {
        this.entityIds = new IntOpenHashSet(nbt.getIntArray("ChargedTargets"));
        this.discharged = nbt.getBoolean("Discharged");
        this.discharging = nbt.getBoolean("Discharging");
    }
}
