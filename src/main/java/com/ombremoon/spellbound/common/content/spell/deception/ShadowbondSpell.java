package com.ombremoon.spellbound.common.content.spell.deception;

import com.ombremoon.spellbound.CommonClass;
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

                } else if (context.getSkillHandler().hasSkill(SkillInit.SHADOW_CHAIN.value())) {
                    return context.getTarget() != null && shadowBond.secondTarget == 0;
                }
            }
            return !context.isRecast() && context.getTarget() != null;
        }).duration(300).fullRecast().shouldPersist();
    }

    private int firstTarget;
    private int secondTarget;
    private boolean canReverse = false;
    private final List<Integer> targetList = new IntArrayList();

    public ShadowbondSpell() {
        super(SpellInit.SHADOWBOND.get(), createShadowbondBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        LivingEntity livingEntity = context.getTarget();
        var handler = context.getSpellHandler();
        var skillHandler = context.getSkillHandler();
        Level level = context.getLevel();
        if (livingEntity == null) return;
        int id = livingEntity.getId();
        this.targetList.add(id);

        if (context.isRecast()) {
            this.secondTarget = id;
        } else {
            this.firstTarget = id;
        }

        if (!level.isClientSide) {
            MobEffectInstance mobEffectInstance = new MobEffectInstance(MobEffects.INVISIBILITY, this.getDuration() - 5, 0, false, false);
            context.getPlayer().addEffect(mobEffectInstance);
            for (Integer entityId : this.targetList) {
                Entity entity = level.getEntity(entityId);
                if (entity instanceof LivingEntity living) {
                    living.addEffect(mobEffectInstance);
                    if (skillHandler.hasSkill(SkillInit.OBSERVANT.value()))
                        handler.addGlowEffect(livingEntity);
                }
            }
        }
    }

    @Override
    protected void onSpellRecast(SpellContext context) {
        super.onSpellRecast(context);
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (this.canReverse) {
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
            }
            endSpell();
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
            for (Integer entityId : this.targetList) {
                Entity entity = level.getEntity(entityId);
                if (entity == null)
                    this.targetList.remove(entityId);
            }

            if (this.ticks == this.getDuration() - 100) {
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

                if (skillHandler.hasSkill(SkillInit.SHADOW_STEP.value()))
                    addTimedAttributeModifier(player, Attributes.MOVEMENT_SPEED, new AttributeModifier(CommonClass.customLocation("shadow_step"), 1.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), 100);

                if (skillHandler.hasSkill(SkillInit.SNEAK_ATTACK.value())) {
                    handler.getListener().addListener(SpellEventListener.Events.PRE_DAMAGE, SNEAK_ATTACK, pre -> {
                        pre.setNewDamage(pre.getOriginalDamage() * 1.5F);
                        handler.getListener().removeListener(SpellEventListener.Events.PRE_DAMAGE, SNEAK_ATTACK);
                    });
                }

                for (Integer entityId : this.targetList) {
                    Entity effectEntity = level.getEntity(entityId);
                    if (effectEntity instanceof LivingEntity living) {
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
            } else if (this.ticks > this.getDuration() - 100) {
                if (skillHandler.hasSkill(SkillInit.REVERSAL.value()) && !this.targetList.isEmpty())
                    this.canReverse = true;
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        var handler = context.getSpellHandler();
        handler.getListener().removeListener(SpellEventListener.Events.PRE_DAMAGE, SNEAK_ATTACK);
    }

    private void teleport(LivingEntity first, LivingEntity second) {
        Vec3 firstPos = first.position();
        Vec3 secondPosPos = second.position();
        first.teleportTo(secondPosPos.x, secondPosPos.y, secondPosPos.z);
        second.teleportTo(firstPos.x, firstPos.y, firstPos.z);
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        compoundTag.putInt("FirstTargetId", this.firstTarget);
        compoundTag.putInt("SecondTargetId", this.secondTarget);
        compoundTag.putBoolean("CanReverse", this.canReverse);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.firstTarget = nbt.getInt("FirstTargetId");
        this.secondTarget = nbt.getInt("SecondTargetId");
        this.canReverse = nbt.getBoolean("CanReverse");
    }
}