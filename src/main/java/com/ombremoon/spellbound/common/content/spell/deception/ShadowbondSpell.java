package com.ombremoon.spellbound.common.content.spell.deception;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.content.entity.living.LivingShadow;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
    private final ResourceLocation SNEAK_ATTACK = CommonClass.customLocation("sneak_attack");
    private final ResourceLocation DISORIENTED = CommonClass.customLocation("disoriented");

    public static Builder<ShadowbondSpell> createShadowbondBuilder() {
        return createSimpleSpellBuilder(ShadowbondSpell.class).castCondition((context, spell) -> {
            if (context.isRecast()) {
                if (spell.canReverse) {
                    return true;
                } else if (context.getSkills().hasSkill(SBSkills.SHADOW_CHAIN.value()) && context.getTarget() != null && context.getTarget().getId() != spell.firstTarget) {
                    return spell.secondTarget == 0;
                } else if (!spell.earlyEnd && !spell.canReverse) {
                    spell.earlyEnd = true;
                    return true;
                }
            }
            return !context.isRecast() && context.getTarget() != null;
        }).duration(300).fullRecast();
    }

    private int firstTarget;
    private int secondTarget;
    private boolean earlyEnd;
    private boolean canReverse = false;
    private List<Integer> targetList = new IntArrayList();

    public ShadowbondSpell() {
        super(SBSpells.SHADOWBOND.get(), createShadowbondBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Level level = context.getLevel();
        LivingEntity livingEntity = context.getTarget();
        var skills = context.getSkills();
        if (livingEntity == null) return;
        int id = livingEntity.getId();
        boolean flag = skills.hasSkill(SBSkills.SHADOW_CHAIN.value());

        if (id > 0) {
            if (context.isRecast() && !this.canReverse && flag && this.secondTarget == 0) {
                this.secondTarget = id;
            } else if (!context.isRecast() && this.firstTarget == 0){
                this.firstTarget = id;
            }
        }

        this.targetList.add(this.firstTarget);
        this.targetList.add(this.secondTarget);
        if (!level.isClientSide) {
            if (!this.canReverse) {
                MobEffectInstance mobEffectInstance = new SBEffectInstance(context.getPlayer(), MobEffects.INVISIBILITY, -1, skills.hasSkill(SBSkills.OBSERVANT.value()), 0, false, false);
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
        var skills = context.getSkills();
        if (this.canReverse || this.earlyEnd) {
            swapTargets(player, level);

            if (this.earlyEnd) {
                this.canReverse = skills.hasSkill(SBSkills.REVERSAL.value()) && !this.targetList.isEmpty();
                handleSwapEffect(player, level, handler, skills);
                if (!this.canReverse) endSpell();
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
        var skills = context.getSkills();
        var handler = context.getSpellHandler();
        if (!level.isClientSide) {
            for (Integer entityId : this.targetList) {
                Entity entity = level.getEntity(entityId);
                if (entity == null)
                    this.targetList.remove(entityId);
            }

            int extension = skills.hasSkill(SBSkills.EVERLASTING_BOND.value()) ? 200 : 100;
            if (this.ticks == this.getDuration() - extension) {
                swapTargets(player, level);
                handleSwapEffect(player, level, handler, skills);
            } else if (this.ticks > this.getDuration() - extension) {
                this.canReverse = skills.hasSkill(SBSkills.REVERSAL.value()) && !this.targetList.isEmpty();
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

    private void handleSwapEffect(Player player, Level level, SpellHandler handler, SkillHolder skills) {
        if (skills.hasSkill(SBSkills.SHADOW_STEP.value()))
            addTimedAttributeModifier(player, Attributes.MOVEMENT_SPEED, new AttributeModifier(CommonClass.customLocation("shadow_step"), 1.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), 100);

        if (skills.hasSkill(SBSkills.SNEAK_ATTACK.value())) {
            addTimedListener(player, SpellEventListener.Events.PRE_DAMAGE, SNEAK_ATTACK, pre -> {
                pre.setNewDamage(pre.getOriginalDamage() * 1.5F);
                handler.getListener().removeListener(SpellEventListener.Events.PRE_DAMAGE, SNEAK_ATTACK);
            }, 100);
        }

        player.removeEffect(MobEffects.INVISIBILITY);
        for (Integer entityId : this.targetList) {
            Entity effectEntity = level.getEntity(entityId);
            if (effectEntity instanceof LivingEntity living) {
                living.removeEffect(MobEffects.INVISIBILITY);
                if (skills.hasSkill(SBSkills.SILENT_EXCHANGE.value()))
                    living.addEffect(new MobEffectInstance(SBEffects.SILENCED, 100, 0, false, true));

                if (skills.hasSkill(SBSkills.SNARE.value()))
                    living.addEffect(new MobEffectInstance(SBEffects.ROOTED, 100, 0, false, true));

                if (skills.hasSkill(SBSkills.DISORIENTED.value())) {
                    living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
                    addTimedListener(living, SpellEventListener.Events.PRE_DAMAGE, DISORIENTED, pre -> {
                        pre.setNewDamage(pre.getOriginalDamage() * 0.8F);
                    }, 100);
                }
            }
        }

        if (skills.hasSkill(SBSkills.LIVING_SHADOW.value())) {
            LivingShadow livingShadow = SBEntities.LIVING_SHADOW.get().create(level);
            livingShadow.setData(SBData.OWNER_ID, player.getId());
            livingShadow.setPos(player.position());
            level.addFreshEntity(livingShadow);
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false));
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