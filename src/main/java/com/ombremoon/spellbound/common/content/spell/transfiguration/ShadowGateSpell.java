package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.entity.spell.ShadowGate;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.api.*;
import com.ombremoon.spellbound.common.magic.api.buff.*;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Map;

//TODO: ADD OPEN/CLOSE ANIMATIONS

public class ShadowGateSpell extends AnimatedSpell {
    private static Builder<ShadowGateSpell> createShadowGateBuilder() {
        return createSimpleSpellBuilder(ShadowGateSpell.class)
                .manaCost(30).castTime(20)
                .duration(context -> 1200)
                .castCondition((context, spell) -> {
                    var skills = context.getSkills();
                    int activePortals = spell.portalInfo.size();
                    boolean hasReach = skills.hasSkill(SBSkills.REACH.value());
                    BlockHitResult hitResult = spell.getTargetBlock(hasReach ? 100 : 50);
                    if (hitResult.getType() == HitResult.Type.MISS || hitResult.getDirection() == Direction.DOWN) return false;

                    BlockPos blockPos = hitResult.getBlockPos().above();
                    if (!context.getLevel().getBlockState(blockPos).isAir()) return false;
                    if (activePortals > 1) {
                        int portalRange = hasReach ? 10000 : 2500;
                        PortalInfo info = spell.portalInfo.get(spell.getPreviousGate());
                        spell.log(info.id);
                        double distance = info.position().distanceToSqr(blockPos.getCenter());
                        if (distance > portalRange) return false;
                    }

                    if (skills.hasSkill(SBSkills.DARKNESS_PREVAILS.value())) return true;
                    int i = context.getLevel().getRawBrightness(blockPos, 0) + context.getLevel().getBrightness(LightLayer.BLOCK, blockPos) - context.getLevel().getSkyDarken();
                    return i <= 4;
                }).fullRecast().skipEndOnRecast();
    }
    private static final ResourceLocation UNWANTED_GUESTS = CommonClass.customLocation("unwanted_guests");

    private final Map<Integer, PortalInfo> portalInfo = new Int2ObjectOpenHashMap<>();

    public ShadowGateSpell() {
        super(SBSpells.SHADOW_GATE.get(), createShadowGateBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Level level = context.getLevel();
        var skills = context.getSkills();
        if (!level.isClientSide) {
            int activePortals = this.portalInfo.size();
            boolean hasReach = context.getSkills().hasSkill(SBSkills.REACH.value());
            BlockHitResult hitResult = this.getTargetBlock(hasReach ? 100 : 50);
            Vec3 vec3 = Vec3.atBottomCenterOf(hitResult.getBlockPos().above());
            ShadowGate shadowGate = SBEntities.SHADOW_GATE.get().create(level);
            if (shadowGate != null) {
                int maxPortals = skills.hasSkill(SBSkills.DUAL_DESTINATION.value()) ? 3 : 2;
                if (activePortals >= maxPortals) {
                    shiftGates(level, shadowGate.getId(), vec3);
                } else {
                    PortalInfo info = new PortalInfo(activePortals, vec3);
                    this.portalInfo.put(shadowGate.getId(), info);
                }
                shadowGate.setOwner(context.getCaster());
                shadowGate.setPos(vec3);
                shadowGate.setYRot(context.getRotation());
                shadowGate.setStartTick(20);
                level.addFreshEntity(shadowGate);
            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            var skills = context.getSkills();
            if (!this.portalInfo.isEmpty()) {
                for (var entry : this.portalInfo.entrySet()) {
                    ShadowGate shadowGate = (ShadowGate) level.getEntity(entry.getKey());
                    if (shadowGate != null) {
                        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, shadowGate.getBoundingBox());
                        List<LivingEntity> teleportList = new ObjectArrayList<>();
                        for (LivingEntity entity : entities) {
                            if (skills.hasSkill(SBSkills.OPEN_INVITATION.value())) {
                                teleportList.add(entity);
                            } else if (isCaster(entity)) {
                                teleportList.add(entity);
                            }
                        }

                        ShadowGate adjacentGate = this.getAdjacentGate(shadowGate, level);
                        if (adjacentGate != null) {
                            for (LivingEntity entity : teleportList) {
                                if (!shadowGate.isOnCooldown(entity)) {
                                    Vec3 position = adjacentGate.position();
                                    adjacentGate.addCooldown(entity);
                                    entity.teleportTo(position.x, position.y, position.z);
                                    if (entity instanceof Player teleportedPlayer)
                                        PayloadHandler.setRotation(teleportedPlayer, teleportedPlayer.getXRot(), adjacentGate.getYRot());

                                    if (skills.hasSkill(SBSkills.BLINK.value()) && isCaster(entity))
                                        addSkillBuff(
                                                caster,
                                                SBSkills.BLINK.value(),
                                                BuffCategory.BENEFICIAL,
                                                SkillBuff.ATTRIBUTE_MODIFIER,
                                                new ModifierData(Attributes.MOVEMENT_SPEED, new AttributeModifier(CommonClass.customLocation("blink"), 1.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                                                100);

                                    if (skills.hasSkillReady(SBSkills.QUICK_RECHARGE.value())) {
                                        context.getSpellHandler().awardMana(20);
                                        addCooldown(SBSkills.QUICK_RECHARGE.value(), 200);
                                    }

                                    if (skills.hasSkill(SBSkills.SHADOW_ESCAPE.value()) && isCaster(entity) && caster.getHealth() < caster.getMaxHealth() * 0.5F && !caster.hasEffect(MobEffects.INVISIBILITY))
                                        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, true));

                                    if (skills.hasSkill(SBSkills.UNWANTED_GUESTS.value()) && !entity.isAlliedTo(context.getCaster())) {
                                        addEventBuff(
                                                entity,
                                                SBSkills.UNWANTED_GUESTS.value(),
                                                BuffCategory.HARMFUL,
                                                SpellEventListener.Events.PRE_DAMAGE,
                                                UNWANTED_GUESTS,
                                                pre -> pre.setNewDamage(pre.getOriginalDamage() * 0.9F),
                                                200);
                                        addSkillBuff(
                                                entity,
                                                SBSkills.UNWANTED_GUESTS.value(),
                                                BuffCategory.HARMFUL,
                                                SkillBuff.SPELL_MODIFIER,
                                                SpellModifier.UNWANTED_GUESTS,
                                                200);
                                    }

                                    if (skills.hasSkill(SBSkills.BAIT_AND_SWITCH.value()) && !entity.isAlliedTo(caster)) {
                                        entity.hurt(level.damageSources().magic(), 10);
                                        SpellUtil.getSpellHandler(entity).consumeMana(10);
                                    }

                                    if (skills.hasSkill(SBSkills.GRAVITY_SHIFT.value())) {
                                        Vec3 lookVec = adjacentGate.getViewVector(1.0F);
                                        entity.setDeltaMovement(lookVec.x, 2, lookVec.z);
                                        entity.hurtMarked = true;
                                        if (isCaster(entity))
                                            entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            this.portalInfo.forEach((integer, portalInfo) -> {
                Entity entity = context.getLevel().getEntity(integer);
                if (entity instanceof ShadowGate shadowGate)
                    shadowGate.setEndTick(20);
            });
        }
    }

    private void shiftGates(Level level, int shadowGateID, Vec3 position) {
        for (var entry : this.portalInfo.entrySet()) {
            var info = entry.getValue();
            if (info.id == 0) {
                ShadowGate shadowGate = (ShadowGate) level.getEntity(entry.getKey());
                if (shadowGate != null)
                    shadowGate.discard();

                this.portalInfo.remove(entry.getKey());
            } else {
                PortalInfo newInfo = new PortalInfo(info.id - 1, info.position);
                this.portalInfo.replace(entry.getKey(), newInfo);
            }
        }
        PortalInfo info = new PortalInfo(this.portalInfo.size(), position);
        this.portalInfo.put(shadowGateID, info);
    }

    private ShadowGate getAdjacentGate(ShadowGate shadowGate, Level level) {
        int activePortals = this.portalInfo.size();
        if (activePortals < 2) return null;

        int id = shadowGate.getId();
        PortalInfo info = this.portalInfo.get(id);
        if (info != null) {
            int portalId = info.id + 1;
            if (portalId >= activePortals) portalId = 0;
            for (var entry : this.portalInfo.entrySet()) {
                if (portalId == entry.getValue().id())
                    return (ShadowGate) level.getEntity(entry.getKey());
            }
        }
        return null;
    }

    private int getPreviousGate() {
        int i = 0;
        for (var entry : this.portalInfo.entrySet()) {
            if (entry.getValue().id() > i)
                i = entry.getValue().id;
        }
        return getGateFromID(i);
    }

    private int getGateFromID(int id) {
        for (var entry : this.portalInfo.entrySet()) {
            if (entry.getValue().id == id)
                return entry.getKey();
        }
        return 0;
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        ListTag listTag = new ListTag();
        for (var entry : this.portalInfo.entrySet()) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("PortalEntityId", entry.getKey());
            nbt.putInt("PortalId", entry.getValue().id());

            Vec3 vec3 = entry.getValue().position();
            nbt.putIntArray("PortalPosition", List.of((int)vec3.x(), (int)vec3.y(), (int)vec3.z()));
            listTag.add(nbt);
        }
        compoundTag.put("PortalInfo", listTag);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag nbt) {
        if (nbt.contains("PortalInfo", 9)) {
            ListTag listTag = nbt.getList("PortalInfo", 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag compoundTag = listTag.getCompound(i);
                int entityId = compoundTag.getInt("PortalEntityId");
                int portalId = compoundTag.getInt("PortalId");
                var posArray = compoundTag.getIntArray("PortalPosition");
                this.portalInfo.put(entityId, new PortalInfo(portalId, new Vec3(posArray[0], posArray[1], posArray[2])));
            }
        }
    }

    private record PortalInfo(int id, Vec3 position) {}
}
