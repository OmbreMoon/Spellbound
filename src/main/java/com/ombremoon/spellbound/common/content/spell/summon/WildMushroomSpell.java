package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.entity.spell.WildMushroom;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.data.StatusHandler;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.SpellModifier;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import com.ombremoon.spellbound.common.magic.events.PlayerKillEvent;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WildMushroomSpell extends SummonSpell {
    private static final UUID PLAYER_KILL = UUID.fromString("a7f94078-08e9-487d-9fd2-07eadba8df28");
    private static final ResourceLocation RECYCLED_LOCATION = CommonClass.customLocation("recycled_regen");
    private static final int MAX_XP = 50;
    private static final int XP_PER_HIT = 5;

    private final Set<LivingEntity> targetsHit = new HashSet<>();
    private WildMushroom mushroom;
    private AABB damageZone;
    private int poisonEssenceExpiry = 0;
    private int awardedXp = 0;
    private int explosionInterval;

    public static Builder<AnimatedSpell> createMushroomBuilder() {
        return createSimpleSpellBuilder().duration(180).manaCost(20).partialRecast();
    }

    public WildMushroomSpell() {
        super(SpellInit.WILD_MUSHROOM_SPELL.get(), createMushroomBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.PLAYER_KILL, PLAYER_KILL, this::playerKill);
        if (!context.getLevel().isClientSide) {
            var mobs = addMobs(context, EntityInit.MUSHROOM.get(), 1);
            if (mobs == null || !mobs.iterator().hasNext()) {
                endSpell();
                context.getSpellHandler().awardMana(this.getManaCost());
                return;
            }

            this.mushroom = (WildMushroom) context.getLevel().getEntity(mobs.iterator().next());
            SkillHandler skillHandler = context.getSkillHandler();
            double radius = skillHandler.hasSkill(SkillInit.VILE_INFLUENCE.value()) ? 3D : 2D;
            this.damageZone = mushroom.getBoundingBox().inflate(radius, 0, radius);
            this.explosionInterval = skillHandler.hasSkill(SkillInit.HASTENED_GROWTH.value()) ? 40 : 60;


            boolean recycledFlag = context.getSpellHandler().getActiveSpells(getSpellType()).size() >= 3;
            ;
            boolean recycledFlag2 = skillHandler.hasSkill(SkillInit.RECYCLED.value());
            if (recycledFlag && recycledFlag2)
                this.addAttributeModifier(context.getPlayer(), AttributesInit.MANA_REGEN, new AttributeModifier(RECYCLED_LOCATION,
                        1.1d,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        if (!context.getLevel().isClientSide) {

            if (poisonEssenceExpiry > 0) poisonEssenceExpiry--;
            this.mushroom.explode();
            Player caster = context.getPlayer();
            SkillHandler skills = context.getSkillHandler();

            List<LivingEntity> entities = caster.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.damageZone,
                    entity -> !entity.is(caster) && !entity.isInvulnerable());

            for (LivingEntity entity : entities) {
                if (skills.hasSkillReady(SkillInit.CATALEPSY.value())) {
                    entity.addEffect(new MobEffectInstance(EffectInit.STUNNED, 100), caster); //TODO: Use Catalepsy effect instead
                }

                if (skills.hasSkill(SkillInit.ENVENOM.value())) {
                    entity.getData(DataInit.STATUS_EFFECTS).increment(StatusHandler.Effect.POISON, 100);
                } else {
                    entity.getData(DataInit.STATUS_EFFECTS).increment(StatusHandler.Effect.POISON, 33);
                }

                entity.hurt(entity.damageSources().explosion(caster, null), calculateDamage(context, entity));
                targetsHit.add(entity);

                if (awardedXp < MAX_XP) {
                    awardedXp++;
                    context.getSkillHandler().awardSpellXp(getSpellType(), XP_PER_HIT);
                    context.getSkillHandler().sync(caster);
                }
            }

            if (!entities.isEmpty() && skills.hasSkillReady(SkillInit.CATALEPSY.value()))
                this.addCooldown(SkillInit.CATALEPSY.value(), 200);

            if (context.getSpellHandler().getActiveSpells(getSpellType()).size() <= 2
                    && this.hasAttributeModifier(context.getPlayer(), AttributesInit.MANA_REGEN, RECYCLED_LOCATION)) {
                this.removeAttributeModifier(context.getPlayer(), AttributesInit.MANA_REGEN, RECYCLED_LOCATION);
            }
        }
    }

    @Override
    protected boolean shouldTickEffect(SpellContext context) {
        return explosionInterval <= 0 || ticks % explosionInterval == 0;
    }

    private float calculateDamage(SpellContext context, LivingEntity target) {
        float damage = 2f;
        SkillHandler handler = context.getSkillHandler();

        if (context.getSkillHandler().hasSkill(SkillInit.DECOMPOSE.value())
                && target.hasEffect(EffectInit.POISON)) damage += (float) (context.getPlayer().getData(DataInit.MANA)/100f);

        if (handler.hasSkill(SkillInit.NATURES_DOMINANCE.value())) damage *= 1f + (0.1f * context.getSpellHandler().getActiveSpells(getSpellType()).size());
        if (handler.hasSkill(SkillInit.POISON_ESSENCE.value()) && poisonEssenceExpiry > ticks) damage *= 1.25f;

        return damage;
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.PLAYER_KILL, PLAYER_KILL);
        if (!context.getLevel().isClientSide) {
            if (context.getSkillHandler().hasSkill(SkillInit.CIRCLE_OF_LIFE.value())) {
                SpellHandler handler = context.getSpellHandler();
                int level = context.getSkillHandler().getSpellLevel(getSpellType());
                handler.awardMana(52 + (2 * (level - 1)));
            }
        }
    }

    private void playerKill(PlayerKillEvent event) {
        for (LivingEntity entity : targetsHit) {
            if (entity.is(event.getDeathEvent().getEntity())) {
                this.poisonEssenceExpiry = ticks + 200;
                if (SpellUtil.getSkillHandler(event.getPlayer()).hasSkill(SkillInit.SYNTHESIS.value())) {
                    this.addTimedModifier(event.getPlayer(), SpellModifier.SYNTHESIS, 120);
                }
                return;
            }
        }
    }
}
