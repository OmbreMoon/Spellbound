package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.common.content.entity.living.MiniMushroom;
import com.ombremoon.spellbound.common.content.entity.spell.WildMushroom;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.ModifierData;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.api.buff.SpellModifier;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.main.CommonClass;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.EventHooks;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

//TODO: CHANGE CIRCLE OF LIFE SKILL TO A TAUNT

public class WildMushroomSpell extends SummonSpell {
    protected static final SpellDataKey<Integer> MUSHROOM = SyncedSpellData.registerDataKey(WildMushroomSpell.class, SBDataTypes.INT.get());
    protected static final SpellDataKey<Integer> MINI_MUSHROOM = SyncedSpellData.registerDataKey(WildMushroomSpell.class, SBDataTypes.INT.get());
    protected static final SpellDataKey<Integer> GIANT_MUSHROOM = SyncedSpellData.registerDataKey(WildMushroomSpell.class, SBDataTypes.INT.get());
    private static final ResourceLocation FUNGAL_HARVEST = CommonClass.customLocation("fungal_harvest");

    public static Builder<WildMushroomSpell> createMushroomBuilder() {
        return createSummonBuilder(WildMushroomSpell.class)
                .mastery(SpellMastery.ADEPT)
                .duration(240)
                .manaCost(30)
                .baseDamage(4.0F)
                .castCondition((context, spell) -> {
                    var skills = context.getSkills();
                    if (skills.hasSkill(SBSkills.LIVING_FUNGUS) && context.getTarget() instanceof WildMushroom mushroom && context.getCaster() == mushroom.getOwner()) {
                        WildMushroomSpell mushroomSpell = mushroom.getSpell();
                        MiniMushroom miniMushroom = mushroomSpell.summonEntity(context, SBEntities.MINI_MUSHROOM.get(), mushroom.position());
                        mushroomSpell.setMiniMushroom(miniMushroom.getId());
                        mushroomSpell.setRemainingTicks(600);
                        mushroom.discard();
                        return false;
                    }

                    return spell.hasValidSpawnPos();
                });
    }

    public WildMushroomSpell() {
        super(SBSpells.WILD_MUSHROOM.get(), createMushroomBuilder());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        builder.define(MUSHROOM, 0);
        builder.define(MINI_MUSHROOM, 0);
        builder.define(GIANT_MUSHROOM, 0);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Level level = context.getLevel();
        if (!level.isClientSide) {
            LivingEntity caster = context.getCaster();
            var skills = context.getSkills();
            WildMushroom mushroom = this.summonEntity(context, SBEntities.MUSHROOM.get());
            this.setMushroom(mushroom.getId());

            if (skills.hasSkill(SBSkills.FUNGAL_HARVEST) && context.hasActiveSpells(3))
                this.addSkillBuff(
                        caster,
                        SBSkills.FUNGAL_HARVEST,
                        BuffCategory.BENEFICIAL,
                        SkillBuff.ATTRIBUTE_MODIFIER,
                        new ModifierData(SBAttributes.MANA_REGEN, new AttributeModifier(FUNGAL_HARVEST, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                );
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Level level = context.getLevel();
        if (!level.isClientSide) {
            LivingEntity caster = context.getCaster();
            var handler = context.getSpellHandler();
            var skills = context.getSkills();
            WildMushroom mushroom = this.getMushroom(context);
            if (mushroom != null) {
                int interval = skills.hasSkill(SBSkills.HASTENED_GROWTH) ? 40 : 60;
                if (this.tickCount % interval == 0) {
                    float damage = this.getBaseDamage();
                    damage *= (float) (1.0 + 0.1F * context.getActiveSpells());
                    List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, mushroom.getBoundingBox().inflate(skills.hasSkill(SBSkills.VILE_INFLUENCE) ? 5 : 3));
                    for (LivingEntity entity : entities) {
                        if (entity.hasEffect(MobEffects.POISON))
                            damage += Mth.floor(5.0 * (handler.getMana() / handler.getMaxMana()));

                        if (!this.isCaster(entity) && this.hurt(entity, damage)) {
                            if (skills.hasSkill(SBSkills.ENVENOM))
                                this.addSkillBuff(
                                        entity,
                                        SBSkills.ENVENOM,
                                        BuffCategory.HARMFUL,
                                        SkillBuff.MOB_EFFECT,
                                        new MobEffectInstance(MobEffects.POISON, 80),
                                        80
                                );

                            if (skills.hasSkill(SBSkills.PARASITIC_FUNGUS) && !entity.hasEffect(SBEffects.TAUNT)) {
                                handler.applyTauntEffect(entity, mushroom.position(), 60);
                            }

                            if (entity.isDeadOrDying()) {
                                if (skills.hasSkill(SBSkills.POISON_ESSENCE))
                                    this.addSkillBuff(
                                            caster,
                                            SBSkills.POISON_ESSENCE,
                                            BuffCategory.BENEFICIAL,
                                            SkillBuff.SPELL_MODIFIER,
                                            SpellModifier.POISON_ESSENCE,
                                            200
                                    );

                                if (skills.hasSkill(SBSkills.SYNTHESIS))
                                    this.addSkillBuff(
                                            caster,
                                            SBSkills.POISON_ESSENCE,
                                            BuffCategory.BENEFICIAL,
                                            SkillBuff.SPELL_MODIFIER,
                                            SpellModifier.SYNTHESIS,
                                            100
                                    );
                            }
                        }
                    }
                }

                if (level.random.nextFloat() < 0.15F && EventHooks.canEntityGrief(level, mushroom))
                    this.spreadSpores(level, caster, mushroom.blockPosition(), skills.hasSkill(SBSkills.VILE_INFLUENCE) ? 6 : 4);
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        Level level = context.getLevel();
        if (!level.isClientSide) {
            LivingEntity caster = context.getCaster();
            var skills = context.getSkills();
            WildMushroom mushroom = this.getMushroom(context);

            if (mushroom != null)
                mushroom.setEndTick(5);

            if (skills.hasSkill(SBSkills.FUNGAL_HARVEST) && context.hasActiveSpells(3))
                this.removeSkillBuff(caster, SBSkills.FUNGAL_HARVEST);

        }
    }

    @Override
    protected int getDuration(SpellContext context) {
        return this.hasEvolvedMushroom(context) ? 600 : super.getDuration(context);
    }

    private boolean hasEvolvedMushroom(SpellContext context) {
        return this.getMiniMushroom(context) != null /*|| this.getGiantMushroom(context) != null*/;
    }

    private void spreadSpores(Level level, LivingEntity caster, BlockPos center, int range) {
        int i = Mth.floor(center.getX());
        int j = Mth.floor(center.getY());
        int k = Mth.floor(center.getZ());
        for (int l = 0; l < 25; l++) {
            int i1 = i + Mth.nextInt(level.random, 0, range) * Mth.nextInt(level.random, -1, 1);
            int j1 = j + Mth.nextInt(level.random, 0, 2) * Mth.nextInt(level.random, -1, 1);
            int k1 = k + Mth.nextInt(level.random, 0, range) * Mth.nextInt(level.random, -1, 1);
            BlockPos blockpos = new BlockPos(i1, j1, k1);
            BlockState blockState = level.getBlockState(blockpos);
            if (blockpos.distToCenterSqr(center.getCenter().x(), (double) blockpos.getY() + 0.5, center.getCenter().z()) < (double) Mth.square(range)
                    && (blockState.isAir() || blockState.canBeReplaced())
                    && SBBlocks.MYCELIUM_CARPET.get().defaultBlockState().canSurvive(level, blockpos)
                    && !blockState.is(SBBlocks.MYCELIUM_CARPET.get())
                    && level.setBlockAndUpdate(blockpos, SBBlocks.MYCELIUM_CARPET.get().defaultBlockState())) {
                level.gameEvent(caster, GameEvent.BLOCK_PLACE, blockpos);
                break;
            }
        }
    }

/*    private void removeSpores(Level level, BlockPos center, int range) {
        BlockPos blockpos = BlockPos.containing(center.getCenter());
        for (BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-range, -2, -range), blockpos.offset(range, 2, range))) {
            BlockState blockState = level.getBlockState(blockpos1);
            if (blockpos1.distToCenterSqr(center.getCenter().x(), (double)blockpos1.getY() + 0.5, center.getCenter().z()) < (double) Mth.square(range)
                    && blockState.is(SBBlocks.MYCELIUM_CARPET.get())) {
                level.setBlockAndUpdate(blockpos1, blockState.setValue(MyceliumCarpetBlock.DESPAWN, true));
            }
        }
    }*/

    private void setMushroom(int mushroom) {
        this.spellData.set(MUSHROOM, mushroom);
    }

    private WildMushroom getMushroom(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.spellData.get(MUSHROOM));
        return entity instanceof WildMushroom mushroom ? mushroom : null;
    }

    private void setMiniMushroom(int mushroom) {
        this.spellData.set(MINI_MUSHROOM, mushroom);
    }

    private MiniMushroom getMiniMushroom(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.spellData.get(MINI_MUSHROOM));
        return entity instanceof MiniMushroom mushroom ? mushroom : null;
    }

/*    public void setGiantMushroom(int mushroom) {
        this.spellData.set(GIANT_MUSHROOM, mushroom);
    }

    private GiantMushroom getGiantMushroom(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.spellData.get(GIANT_MUSHROOM));
        return entity instanceof GiantMushroom mushroom ? mushroom : null;
    }*/
}
