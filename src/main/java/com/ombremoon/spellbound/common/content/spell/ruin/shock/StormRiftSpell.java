package com.ombremoon.spellbound.common.content.spell.ruin.shock;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.entity.spell.ShadowGate;
import com.ombremoon.spellbound.common.content.entity.spell.StormRift;
import com.ombremoon.spellbound.common.init.SBDamageTypes;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.buff.*;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import com.ombremoon.spellbound.util.portal.PortalInfo;
import com.ombremoon.spellbound.util.portal.PortalMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class StormRiftSpell extends AnimatedSpell {
    public static Builder<StormRiftSpell> createStormRiftBuilder() {
        return createSimpleSpellBuilder(StormRiftSpell.class)
                .manaCost(20)
                .duration(context -> 400)
                .castCondition((context, spell) -> {
                    int activePortals = spell.portalMap.size();
                    BlockPos blockPos = spell.getSpawnPos(50);
                    if (blockPos == null) return false;

                    if (!context.getLevel().getBlockState(blockPos).isAir()) return false;
                    if (activePortals > 1) {
                        PortalInfo info = spell.portalMap.get(spell.portalMap.getPreviousPortal());
                        double distance = info.position().distanceToSqr(blockPos.getCenter());
                        return !(distance > 2500);
                    }

                    return true;
                })
                .fullRecast().skipEndOnRecast();
    }

    private final PortalMap<StormRift> portalMap = new PortalMap<>();

    public StormRiftSpell() {
        super(SBSpells.STORM_RIFT.get(), createStormRiftBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            this.summonEntity(context, SBEntities.STORM_RIFT.get(), 20, stormRift -> {
                this.portalMap.summonPortal(stormRift, 2, 0);
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
                    StormRift stormRift = (StormRift) level.getEntity(entry.getKey());
                    if (stormRift != null) {
                        List<Entity> teleportList = level.getEntities(caster, stormRift.getBoundingBox(), EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE));

                        for (Entity entity : teleportList) {
                            LivingEntity livingEntity = (LivingEntity) entity;
                            if (this.portalMap.attemptTeleport(livingEntity, stormRift)) {
                                this.hurt(livingEntity, SBDamageTypes.RUIN_SHOCK, 10F);
                            } else if (skills.hasSkill(SBSkills.DISPLACEMENT_FIELD) && !stormRift.isOnCooldown(livingEntity)) {

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
                if (entity instanceof StormRift stormRift)
                    stormRift.setEndTick(20);
            });
        }
    }
}
