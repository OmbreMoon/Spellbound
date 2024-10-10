package com.ombremoon.spellbound.common.content.spell.deception;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.UUID;

public class ShadowbondSpell extends AnimatedSpell {
    private final UUID SNEAK_ATTACK = UUID.fromString("c53db9d5-11f4-46ee-b44c-28bc92a2f170");
    private final UUID DISORIENTED = UUID.fromString("77d4c9dc-d806-4f15-a293-1160cf9fb874");

    public static AnimatedSpell.Builder<AnimatedSpell> createShadowbondBuilder() {
        return createSimpleSpellBuilder().castCondition((context, spell) -> {
            if (context.isRecast()) {
                ShadowbondSpell shadowBond = (ShadowbondSpell) spell;
                if (shadowBond.canReverse) {
                    return true;
                } else if (context.getSkillHandler().hasSkill(SkillInit.SHADOW_CHAIN.value()) && context.getTarget() != null && context.getTarget().getId() != shadowBond.firstTarget) {
                    return shadowBond.secondTarget == 0;
                } else {
                    shadowBond.earlyEnd = true;
                    return true;
                }
            }
            return !context.isRecast() && context.getTarget() != null;
        }).duration(300).fullRecast().shouldPersist();
    }

    private int firstTarget;
    private int secondTarget;
    private boolean earlyEnd;
    private boolean canReverse = false;
    private List<Integer> targetList = new IntArrayList();

    public ShadowbondSpell() {
        super(SpellInit.SHADOWBOND.get(), createShadowbondBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Level level = context.getLevel();
        LivingEntity livingEntity = context.getTarget();
        var skillHandler = context.getSkillHandler();
        if (livingEntity == null) return;
        int id = livingEntity.getId();

        if (id > 0) {
            if (context.isRecast()) {
                this.secondTarget = id;
            } else {
                this.firstTarget = id;
            }
        }

        this.targetList.add(this.firstTarget);
        this.targetList.add(this.secondTarget);
        if (!level.isClientSide) {
            if (!this.canReverse) {
                MobEffectInstance mobEffectInstance = new SBEffectInstance(context.getPlayer(), MobEffects.INVISIBILITY, -1, skillHandler.hasSkill(SkillInit.OBSERVANT.value()), 0, false, false);
                context.getPlayer().addEffect(mobEffectInstance);
                for (Integer entityId : this.targetList) {
                    Entity entity = level.getEntity(entityId);
                    if (entity instanceof LivingEntity living)
                        living.addEffect(mobEffectInstance);
                }
            }
        }
    }

    @Override
    protected void onSpellRecast(SpellContext context) {
        super.onSpellRecast(context);
        Player player = context.getPlayer();
        Level level = context.getLevel();
        var handler = context.getSpellHandler();
        var skillHandler = context.getSkillHandler();
        if (this.canReverse || this.earlyEnd) {
            swapTargets(player, level);

            if (this.earlyEnd) {
                this.canReverse = skillHandler.hasSkill(SkillInit.REVERSAL.value()) && !this.targetList.isEmpty();
                handleSwapEffect(player, level, handler, skillHandler);
            } else {
                endSpell();
            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Player player = context.getPlayer();
        Level level = context.getLevel();
        var skillHandler = context.getSkillHandler();
        var handler = context.getSpellHandler();
        if (!level.isClientSide) {
            log(this.ticks);
            for (Integer entityId : this.targetList) {
                Entity entity = level.getEntity(entityId);
                if (entity == null)
                    this.targetList.remove(entityId);
            }

            int extension = skillHandler.hasSkill(SkillInit.EVERLASTING_BOND.value()) ? 200 : 100;
            if (this.ticks == this.getDuration() - extension) {
                swapTargets(player, level);
                handleSwapEffect(player, level, handler, skillHandler);
            } else if (this.ticks > this.getDuration() - extension) {
                this.canReverse = skillHandler.hasSkill(SkillInit.REVERSAL.value()) && !this.targetList.isEmpty();
            }
        }
        if (this.earlyEnd && this.ticks >= 100) {
            endSpell();
        }
    }

    private void teleport(LivingEntity first, LivingEntity second) {
        Vec3 firstPos = first.position();
        Vec3 secondPosPos = second.position();
        first.teleportTo(secondPosPos.x, secondPosPos.y, secondPosPos.z);
        second.teleportTo(firstPos.x, firstPos.y, firstPos.z);
    }

    private void swapTargets(Player player, Level level) {
        Vec3 playerPos = player.position();
        Entity entity = level.getEntity(this.firstTarget);
        Entity secondEntity = level.getEntity(this.secondTarget);
        if (entity instanceof LivingEntity living && secondEntity instanceof LivingEntity secondLiving) {
            Vec3 firstPos = living.position();
            Vec3 secondPos = secondLiving.position();
            player.teleportTo(secondPos.x, secondPos.y, secondPos.z);
            secondEntity.teleportTo(firstPos.x, firstPos.y, firstPos.z);
            living.teleportTo(playerPos.x, playerPos.y, playerPos.z);
        } else if (entity instanceof LivingEntity living) {
            teleport(player, living);
        } else if (secondEntity instanceof LivingEntity secondLiving) {
            teleport(player, secondLiving);
        } else {
            endSpell();
        }
    }

    private void handleSwapEffect(Player player, Level level, SpellHandler handler, SkillHandler skillHandler) {
        if (skillHandler.hasSkill(SkillInit.SHADOW_STEP.value()))
            addTimedAttributeModifier(player, Attributes.MOVEMENT_SPEED, new AttributeModifier(CommonClass.customLocation("shadow_step"), 1.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), 100);

        if (skillHandler.hasSkill(SkillInit.SNEAK_ATTACK.value())) {
            addTimedListener(player, SpellEventListener.Events.PRE_DAMAGE, SNEAK_ATTACK, pre -> {
                log(pre.getOriginalDamage());
                pre.setNewDamage(pre.getOriginalDamage() * 1.5F);
                log(pre.getNewDamage());
                handler.getListener().removeListener(SpellEventListener.Events.PRE_DAMAGE, SNEAK_ATTACK);
            }, 100);
        }

        player.removeEffect(MobEffects.INVISIBILITY);
        for (Integer entityId : this.targetList) {
            Entity effectEntity = level.getEntity(entityId);
            if (effectEntity instanceof LivingEntity living) {
                living.removeEffect(MobEffects.INVISIBILITY);
                if (skillHandler.hasSkill(SkillInit.SILENT_EXCHANGE.value()))
                    living.addEffect(new MobEffectInstance(EffectInit.SILENCED, 100, 0, false, true));

                if (skillHandler.hasSkill(SkillInit.SNARE.value()))
                    living.addEffect(new MobEffectInstance(EffectInit.ROOTED, 100, 0, false, true));

                if (skillHandler.hasSkill(SkillInit.DISORIENTED.value())) {
                    living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
                    addTimedListener(living, SpellEventListener.Events.PRE_DAMAGE, DISORIENTED, pre -> {
                        pre.setNewDamage(pre.getOriginalDamage() * 0.8F);
                    }, 100);
                }
            }
        }
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
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.firstTarget = nbt.getInt("FirstTargetId");
        this.secondTarget = nbt.getInt("SecondTargetId");
        this.targetList = IntArrayList.of(nbt.getIntArray("TargetList"));
        this.canReverse = nbt.getBoolean("CanReverse");
    }
}