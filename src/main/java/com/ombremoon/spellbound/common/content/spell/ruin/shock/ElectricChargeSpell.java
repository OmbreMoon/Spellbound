package com.ombremoon.spellbound.common.content.spell.ruin.shock;

import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
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

//TODO: FIX BUG - DISCHARGE TICK SOMETIMES WILL

public class ElectricChargeSpell extends AnimatedSpell {
    private static final SpellDataKey<Integer> DISCHARGE_TICK = SyncedSpellData.define(ElectricChargeSpell.class, DataTypeInit.INT.get());
    public static Builder<AnimatedSpell> createElectricChargeBuilder() {
        return createSimpleSpellBuilder().duration(200).castType(CastType.CHARGING).castCondition((context, spell) -> {
            ElectricChargeSpell electricCharge = (ElectricChargeSpell) spell;
            Entity entity = context.getTarget();
            if (electricCharge.discharging) return false;
            if (entity != null) {
                return !electricCharge.entityIds.contains(entity.getId());
            } else {
                if (electricCharge.entityIds.size() >= 3) electricCharge.discharged = true;
                return context.isRecast();
            }
        }).fullRecast().updateInterval(1);
    }

    private Set<Integer> entityIds = new IntOpenHashSet();
    private boolean discharged;
    private boolean discharging;

    public ElectricChargeSpell() {
        super(SpellInit.ELECTRIC_CHARGE.get(), createElectricChargeBuilder());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        super.defineSpellData(builder);
        builder.define(DISCHARGE_TICK, 0);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        LivingEntity livingEntity = context.getTarget();
        if (livingEntity != null && this.entityIds.size() < 3)
            this.entityIds.add(livingEntity.getId());
    }

    @Override
    protected void onSpellRecast(SpellContext context) {
        super.onSpellRecast(context);
        Level level = context.getLevel();
        Player player = context.getPlayer();
        var handler = context.getSpellHandler();
        var skills = context.getSkills();
        if (skills.hasSkill(SkillInit.AMPLIFY.value())) return;
        if (context.getTarget() == null || this.discharged) {
            for (Integer entityId : this.entityIds) {
                Entity entity = level.getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    discharge(player, level, livingEntity, handler, skills);
                }
            }
            endSpell();
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Player player = context.getPlayer();
        Level level = context.getLevel();
        var handler = context.getSpellHandler();
        var skills = context.getSkills();
        log(this.ticks);
        if (skills.hasSkill(SkillInit.AMPLIFY.value())) {
            if ((context.isRecast() && context.getTarget() == null) || this.discharged) {
                this.discharging = true;
                if (handler.isCastKeyDown()) {
                    incrementTick();
                } else if (this.getDischargeTick() >= 1 && !handler.isCastKeyDown()) {
                    for (Integer entityId : this.entityIds) {
                        Entity entity = level.getEntity(entityId);
                        if (entity instanceof LivingEntity livingEntity) {
                            discharge(player, level, livingEntity, handler, skills);
                        }
                    }
                    endSpell();
                }
            }
        }
    }

    private void discharge(Player player, Level level, LivingEntity target, SpellHandler handler, SkillHandler skills) {
        if (!level.isClientSide) {
            float damage = 10;
            if (skills.hasSkill(SkillInit.OSCILLATION.value())) {
                for (ItemStack itemStack : player.getInventory().items) {
                    if (itemStack.is(ItemInit.STORM_SHARD.get())) {
                        for (int i = 0; i < Math.min(itemStack.getCount(), 10); i++) {
                            damage *= 1.05F;
                        }
                    }
                }
            }
            if (this.getDischargeTick() >= 60)
                damage *= 2;

            boolean stormSurgeFlag = skills.hasSkill(SkillInit.STORM_SURGE.value());
            var entities = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            if (target.hurt(BoxUtil.sentinelDamageSource(level, DamageTypeInit.RUIN_SHOCK, player), potency(damage))) {
                if (target.isDeadOrDying()) {
                    if (stormSurgeFlag)
                        handler.awardMana(20 + ((skills.getSpellLevel(getSpellType()) - 1) * 5));

                    if (skills.hasSkill(SkillInit.UNLEASHED_STORM.value())) {
                        for (LivingEntity targetEntity : entities) {
                            if (!isCaster(targetEntity))
                                targetEntity.hurt(BoxUtil.sentinelDamageSource(level, DamageTypeInit.RUIN_SHOCK, player), potency(damage / 2));
                        }
                    }

                    if (skills.hasSkill(SkillInit.CYCLONIC_FURY.value()))
                        player.addItem(new ItemStack(ItemInit.STORM_SHARD.get()));
                }
            }

            if (skills.hasSkill(SkillInit.ELECTRIFICATION.value()))
                log("electrification");

            if (skills.hasSkill(SkillInit.HIGH_VOLTAGE.value()) && player.getOffhandItem().is(ItemInit.STORM_SHARD.get())) {
                MobEffectInstance mobEffectInstance = new MobEffectInstance(EffectInit.STUNNED, 60, 0, false, false);
                target.addEffect(mobEffectInstance);
                for (LivingEntity paralysisTarget : entities) {
                    if (!isCaster(paralysisTarget))
                        paralysisTarget.addEffect(mobEffectInstance);
                }
                player.getOffhandItem().shrink(1);
            }

            if (skills.hasSkill(SkillInit.ALTERNATING_CURRENT.value())) {
                if (RandomUtil.percentChance(potency(0.03F)) && target.getHealth() < player.getHealth() * 2) {
                    target.kill();
                    if (stormSurgeFlag) player.addItem(new ItemStack(ItemInit.STORM_SHARD.get()));
                } else {
                    player.hurt(BoxUtil.sentinelDamageSource(level, DamageTypeInit.RUIN_SHOCK, player), player.getMaxHealth() * 0.05F);
                }
            }

            if (skills.hasSkill(SkillInit.CHAIN_REACTION.value())) {
                for (LivingEntity livingEntity : entities) {
                    if (!isCaster(livingEntity) && !this.entityIds.contains(livingEntity.getId())) {
                        this.entityIds.add(livingEntity.getId());
                        discharge(player, level, livingEntity, handler, skills);
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
    public void load(CompoundTag nbt) {
        this.entityIds = new IntOpenHashSet(nbt.getIntArray("ChargedTargets"));
        this.discharged = nbt.getBoolean("Discharged");
        this.discharging = nbt.getBoolean("Discharging");
    }
}