package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.entity.spell.ShadowGate;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.SpellModifier;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import java.util.UUID;

//TODO: ADD OPEN/CLOSE ANIMATIONS

public class ShadowGateSpell extends AnimatedSpell {
    private static Builder<AnimatedSpell> createShadowGateBuilder() {
        return createSimpleSpellBuilder().manaCost(30).castTime(20).duration(1200).castCondition((context, spell) -> {
            if (spell instanceof ShadowGateSpell shadowGate) {
                var skills = context.getSkills();
                int activePortals = shadowGate.portalInfo.size();
                if ((!skills.hasSkill(SkillInit.DUAL_DESTINATION.value()) && activePortals == 2) || activePortals == 3)
                    return false;

                boolean hasReach = skills.hasSkill(SkillInit.REACH.value());
                BlockHitResult hitResult = shadowGate.getTargetBlock(hasReach ? 100 : 50);
                if (hitResult.getType() == HitResult.Type.MISS || hitResult.getDirection() == Direction.DOWN) return false;

                BlockPos blockPos = hitResult.getBlockPos().above();
                if (!context.getLevel().getBlockState(blockPos).isAir()) return false;
                if (activePortals > 1) {
                    int portalRange = hasReach ? 10000 : 2500;
                    PortalInfo info = shadowGate.portalInfo.get(shadowGate.getPreviousGate());
                    double distance = info.position().distanceToSqr(blockPos.getCenter());
                    if (distance > portalRange) return false;
                }

                if (skills.hasSkill(SkillInit.DARKNESS_PREVAILS.value())) return true;
                int i = context.getLevel().getRawBrightness(blockPos, 0) + context.getLevel().getBrightness(LightLayer.BLOCK, blockPos) - context.getLevel().getSkyDarken();
                return i <= 4;
            }
            return false;
        }).fullRecast().skipEndOnRecast();
    }
    private static final UUID UNWANTED_GUESTS = UUID.fromString("ba20a80b-aa41-4598-9ab5-c583a80b6a09");

    private final Map<Integer, PortalInfo> portalInfo = new Int2ObjectOpenHashMap<>();

    public ShadowGateSpell() {
        super(SpellInit.SHADOW_GATE.get(), createShadowGateBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Level level = context.getLevel();
        if (!level.isClientSide) {
            int activePortals = this.portalInfo.size();
            boolean hasReach = context.getSkills().hasSkill(SkillInit.REACH.value());
            BlockHitResult hitResult = this.getTargetBlock(hasReach ? 100 : 50);
            Vec3 vec3 = Vec3.atBottomCenterOf(hitResult.getBlockPos().above());
            ShadowGate shadowGate = EntityInit.SHADOW_GATE.get().create(context.getLevel());
            if (shadowGate != null) {
                PortalInfo info = new PortalInfo(activePortals, vec3);
                this.portalInfo.put(shadowGate.getId(), info);
                shadowGate.setOwner(context.getPlayer());
                shadowGate.setPos(vec3.x(), vec3.y(), vec3.z());
                shadowGate.setYRot(context.getRotation());
                shadowGate.setStartTick(20);
                level.addFreshEntity(shadowGate);
            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Player player = context.getPlayer();
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
                            if (skills.hasSkill(SkillInit.OPEN_INVITATION.value())) {
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

                                    if (skills.hasSkill(SkillInit.BLINK.value()) && isCaster(entity))
                                        addTimedAttributeModifier(entity, Attributes.MOVEMENT_SPEED, new AttributeModifier(CommonClass.customLocation("blink"), 1.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), 100);

                                    if (skills.hasSkillReady(SkillInit.QUICK_RECHARGE.value())) {
                                        context.getSpellHandler().awardMana(20);
                                        addCooldown(SkillInit.QUICK_RECHARGE.value(), 200);
                                    }

                                    if (skills.hasSkill(SkillInit.SHADOW_ESCAPE.value()) && isCaster(entity) && player.getHealth() < player.getMaxHealth() * 0.5F && !player.hasEffect(MobEffects.INVISIBILITY))
                                        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, true));

                                    if (skills.hasSkill(SkillInit.UNWANTED_GUESTS.value()) && !entity.isAlliedTo(context.getPlayer())) {
                                        addTimedListener(entity, SpellEventListener.Events.PRE_DAMAGE, UNWANTED_GUESTS, event -> {
                                            event.setNewDamage(event.getOriginalDamage() * .9F);
                                        }, 200);
                                        addTimedModifier(entity, SpellModifier.UNWANTED_GUESTS, 200);
                                    }

                                    if (skills.hasSkill(SkillInit.BAIT_AND_SWITCH.value()) && !entity.isAlliedTo(player)) {
                                        entity.hurt(level.damageSources().magic(), 10);
                                        SpellUtil.getSpellHandler(entity).consumeMana(10);
                                    }

                                    if (skills.hasSkill(SkillInit.GRAVITY_SHIFT.value())) {
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

    public ShadowGate getAdjacentGate(ShadowGate shadowGate, Level level) {
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
                return entry.getKey();
        }
        return i;
    }

    private record PortalInfo(int id, Vec3 position) {}
}
