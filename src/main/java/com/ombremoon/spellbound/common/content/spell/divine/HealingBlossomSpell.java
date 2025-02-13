package com.ombremoon.spellbound.common.content.spell.divine;

import com.ombremoon.spellbound.common.content.entity.spell.HealingBlossom;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.ModifierData;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.buff.events.DamageEvent;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public class HealingBlossomSpell extends AnimatedSpell {
    private static final SpellDataKey<Integer> BLOSSOM_ID = SyncedSpellData.registerDataKey(HealingBlossomSpell.class, SBDataTypes.INT.get());
    private static final ResourceLocation PETAL_SHIELD = CommonClass.customLocation("petal_shield");
    private static final ResourceLocation PLAYER_DAMAGE = CommonClass.customLocation("rebirth_damage");
    private boolean fastBloomed = false;
    private boolean hasBursted = false;

    private static Builder<HealingBlossomSpell> createHealingBlossomSpell() {
        return createSimpleSpellBuilder(HealingBlossomSpell.class)
                .manaCost(30).castTime(20)
                .duration(context -> {
                    //TODO: Move this to new addition duration modifiers
                    int duration = 400;
                    if (context.getSkills().hasSkill(SBSkills.BLOOM)) duration -= 200;
                    if (context.getSkills().hasSkill(SBSkills.ETERNAL_SPRING)) duration += 100;
                    return duration;
                }).castCondition((context, spell) -> spell.getSpawnPos(5) != null)
                .fullRecast();
    }

    public HealingBlossomSpell() {
        super(SBSpells.HEALING_BLOSSOM.get(), createHealingBlossomSpell());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        super.defineSpellData(builder);
        builder.define(BLOSSOM_ID, 0);
    }

    private void setBlossom(HealingBlossom blossom) {
        this.spellData.set(BLOSSOM_ID, blossom.getId());
    }

    private HealingBlossom getBlossom(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.spellData.get(BLOSSOM_ID));
        return (entity instanceof HealingBlossom blossom) ? blossom : null;
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        if (context.getLevel().isClientSide) return;
        SkillHolder skills = context.getSkills();
        this.summonEntity(context, SBEntities.HEALING_BLOSSOM.get(), 5, blossom -> {
            if (skills.hasSkill(SBSkills.BLOOM)) {
                blossom.fastBloom();
                this.fastBloomed = true;
            }
            setBlossom(blossom);
        });

        if (skills.hasSkill(SBSkills.REBIRTH)) context.getSpellHandler().getListener().addListener(SpellEventListener.Events.PRE_DAMAGE,
                PLAYER_DAMAGE, this::onDamagePre);
        if (skills.hasSkill(SBSkills.PETAL_SHIELD)) addSkillBuff(
                context.getCaster(),
                SBSkills.PETAL_SHIELD.value(),
                BuffCategory.BENEFICIAL,
                SkillBuff.ATTRIBUTE_MODIFIER,
                new ModifierData(Attributes.ARMOR, new AttributeModifier(PETAL_SHIELD,
                        1.2f,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                this.getDuration()
        );
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        if (context.getLevel().isClientSide) return;
        if ((ticks+2) % 30 != 0 && ticks % 20 != 0) return;
        HealingBlossom blossom = getBlossom(context);
        if (blossom == null) return;
        LivingEntity caster = context.getCaster();
        SkillHolder skills = context.getSkills();
        Level level = context.getLevel();

        if (!this.hasBursted && skills.hasSkill(SBSkills.BURST_OF_LIFE)) {
            context.getCaster().heal(4f);
            this.hasBursted = true;
        }

        //Damage is done separately to healing to sync with animation better
        List<LivingEntity> effectedEntities = level.getEntitiesOfClass(LivingEntity.class, blossom.getBoundingBox().inflate(10));
        if ((ticks+2) % 30 == 0) {
            for (LivingEntity entity : effectedEntities) {
                if (skills.hasSkill(SBSkills.THORNY_VINES) && canAttack(entity))
                    this.hurt(entity, SBDamageTypes.SB_GENERIC, 4f);
            }
            return;
        }

        float healingAmount = 2f;
        for (LivingEntity entity : effectedEntities) {
            if (entity.is(caster)) {
                if (skills.hasSkill(SBSkills.VERDANT_RENEWAL)) this.cleanseCaster();
                if (skills.hasSkill(SBSkills.FLOURISHING_GROWTH)) {
                    float maxHp = caster.getMaxHealth();
                    float currentHp = caster.getHealth();
                    float overflowHp = (currentHp + healingAmount) - maxHp;

                    if (overflowHp > 0) {
                        context.getSpellHandler().awardMana(overflowHp * 5f);
                    }
                }
                caster.heal(healingAmount);
            } else if (entity instanceof Player && skills.hasSkill(SBSkills.FLOWER_FIELD)) {
                entity.heal(healingAmount/2f);
            }
        }
    }

    private boolean canAttack(LivingEntity entity) {
        return entity instanceof Mob mob
                && mob.getType().getCategory() == MobCategory.MONSTER;
    }

    @Override
    protected boolean shouldTickSpellEffect(SpellContext context) {
        return ((fastBloomed && ticks >= 20) || ticks >= 200);
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        if (!context.getLevel().isClientSide) return;
        HealingBlossom blossom = getBlossom(context);
        if (blossom != null) blossom.setEndTick(20);
    }

    private void onDamagePre(DamageEvent.Pre pre) {
        HealingBlossom blossom = getBlossom(getCastContext());
        if (blossom == null) return;
        if (!blossom.isEmpowered()) return;

        float currentHp = pre.getCaster().getHealth();
        if (currentHp - pre.getNewDamage() <= 0) {
            pre.setNewDamage(0f);
            pre.getCaster().setHealth(pre.getCaster().getMaxHealth()/2f);
            blossom.setEmpowered(false);
            if (pre.getCaster() instanceof Player player)
                shakeScreen(player); //TODO: Sort intensity and add revive anim
        }
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("FastBloomed", fastBloomed);
        compoundTag.putBoolean("HasBursted", hasBursted);
        return compoundTag;
    }

    @Override
    public void loadData(CompoundTag nbt) {
        this.fastBloomed = nbt.getBoolean("FastBloomed");
        this.hasBursted = nbt.getBoolean("HasBursted");
    }
}
