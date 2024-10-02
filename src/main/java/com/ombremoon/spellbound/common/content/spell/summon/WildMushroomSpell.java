package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.data.StatusHandler;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import com.ombremoon.spellbound.common.magic.events.PlayerKillEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WildMushroomSpell extends SummonSpell {
    private static final UUID PLAYER_KILL = UUID.fromString("a7f94078-08e9-487d-9fd2-07eadba8df28");
    private static final ResourceLocation RECYCLED_LOCATION = CommonClass.customLocation("recycled_regen");

    private final Set<LivingEntity> targetsHit = new HashSet<>();
    private AABB damageZone;
    private int poisonEssenceExpiry = 0;
    private int awardedXp = 0;
    private int explosionInterval;
    private static final int MAX_XP = 5;

    public static Builder<AnimatedSpell> createMushroomBuilder() {
        return createSimpleSpellBuilder().setDuration(180).setManaCost(20);
    }

    public WildMushroomSpell() {
        super(SpellInit.WILD_MUSHROOM_SPELL.get(), createMushroomBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        var mobs = addMobs(context, EntityInit.MUSHROOM.get(), 1);
        if (mobs == null || !mobs.iterator().hasNext()) {
            endSpell();
            context.getSpellHandler().awardMana(this.getManaCost());
            context.getSpellHandler().sync();
            return;
        }
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.PLAYER_KILL, PLAYER_KILL, this::playerKill);

        Entity mushroom = context.getLevel().getEntity(mobs.iterator().next());
        SkillHandler skillHandler = context.getSkillHandler();
        double radius = skillHandler.hasSkill(SkillInit.VILE_INFLUENCE.value()) ? 3D : 2D;
        this.damageZone = mushroom.getBoundingBox().inflate(radius, 0, radius);
        this.explosionInterval = skillHandler.hasSkill(SkillInit.HASTENED_GROWTH.value()) ? 40 : 60;


        boolean recycledFlag = context.getSpellHandler().getActiveSpells(getSpellType()).size() >= 3;;
        boolean recycledFlag2 = skillHandler.hasSkill(SkillInit.RECYCLED.value());
        boolean recyledFlag3 = !this.hasAttributeModifier(context.getPlayer(), AttributesInit.MANA_REGEN, RECYCLED_LOCATION);
        if (recycledFlag && recycledFlag2 && recyledFlag3)
            this.addAttributeModifier(context.getPlayer(), AttributesInit.MANA_REGEN, new AttributeModifier(RECYCLED_LOCATION,
                    1.1d,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        if (poisonEssenceExpiry > 0) poisonEssenceExpiry--;
        float intervalProgress = ticks % explosionInterval;
        if (ticks % explosionInterval == 0) {
            Player caster = context.getPlayer();

            List<LivingEntity> entities = caster.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.damageZone,
                    entity -> !entity.is(caster) && !entity.isInvulnerable());

            for (LivingEntity entity : entities) {
                entity.getData(DataInit.STATUS_EFFECTS).increment(StatusHandler.Effect.POISON, 50);
                entity.hurt(entity.damageSources().explosion(caster, null), calculateDamage(context, entity));
                targetsHit.add(entity);

                if (awardedXp < MAX_XP) {
                    awardedXp++;
                    context.getSkillHandler().awardSpellXp(getSpellType(), 1);
                    context.getSkillHandler().sync(caster);
                }
            }
        }
        //TODO: fix this particle mess like seriously who the fuck wrote this???
        if (intervalProgress <= 12 && intervalProgress % 6 == 0) {
            Vec3 center = damageZone.getCenter();
            for (double i = 1; i <= 20; i++) {
                for (double j = 1; j <= 5; j++) {
                    double rot = Math.toRadians(i*18);
                    Vec3 pos = new Vec3(center.x + ((intervalProgress/6)+2-(j/2.5)) * Math.cos(rot),
                            damageZone.minY + (j/4),
                            center.z + ((intervalProgress/6)+2-(j/2.5)) * Math.sin(rot));

                    ((ServerLevel) context.getLevel()).sendParticles(
                            ParticleTypes.FLAME,
                            pos.x, pos.y, pos.z,
                            1, 0, 0, 0, 0);
                    }
                }
        }

        if (context.getSpellHandler().getActiveSpells(getSpellType()).size() <= 2
                && this.hasAttributeModifier(context.getPlayer(), AttributesInit.MANA_REGEN, RECYCLED_LOCATION)) {
            this.removeAttributeModifier(context.getPlayer(), AttributesInit.MANA_REGEN, RECYCLED_LOCATION);
        }
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
        if (context.getSkillHandler().hasSkill(SkillInit.CIRCLE_OF_LIFE.value())) {
            SpellHandler handler = context.getSpellHandler();
            int level = context.getSkillHandler().getSpellLevel(getSpellType());
            handler.awardMana(52 + (2 * (level-1)));
            handler.sync();
        }

        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.PLAYER_KILL, PLAYER_KILL);
    }

    private void playerKill(PlayerKillEvent event) {
        for (LivingEntity entity : targetsHit) {
            if (entity.is(event.getDeathEvent().getEntity())) {
                this.poisonEssenceExpiry = ticks + 200;
                return;
            }
        }
    }
}
