package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.common.content.entity.ShadowGate;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Map;

public class ShadowGateSpell extends AnimatedSpell {
    private static Builder<AnimatedSpell> createShadowGateBuilder() {
        return createSimpleSpellBuilder().castTime(20).duration(400).castCondition((context, spell) -> {
            if (spell instanceof ShadowGateSpell shadowGate) {
                int activePortals = shadowGate.portalInfo.size();
                if ((!context.getSkillHandler().hasSkill(SkillInit.DUAL_DESTINATION.value()) && activePortals == 2) || activePortals == 3)
                    return false;

                BlockHitResult hitResult = context.getLevel().clip(setupRayTraceContext(context.getPlayer(), 20, ClipContext.Fluid.NONE));
                if (hitResult.getType() == HitResult.Type.MISS || hitResult.getDirection() == Direction.DOWN) return false;

                BlockPos blockPos = hitResult.getBlockPos().above();
                if (activePortals > 1) {
                    int portalRange = context.getSkillHandler().hasSkill(SkillInit.REACH.value()) ? 625 : 2500;
                    PortalInfo info = shadowGate.portalInfo.get(shadowGate.getPreviousGate());
                    if (info.position().distanceToSqr(blockPos.getCenter()) > portalRange) return false;
                }

                if (context.getSkillHandler().hasSkill(SkillInit.DARKNESS_PREVAILS.value())) return true;
                int i = context.getLevel().getRawBrightness(blockPos, 0) - context.getLevel().getSkyDarken();
                return i <= 4;
            }
            return false;
        }).fullRecast().shouldPersist();
    }

    private final Map<Integer, PortalInfo> portalInfo = new Int2ObjectOpenHashMap<>();

    public ShadowGateSpell() {
        super(SpellInit.SHADOW_GATE.get(), createShadowGateBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        int activePortals = this.portalInfo.size();
        BlockHitResult hitResult = context.getLevel().clip(setupRayTraceContext(context.getPlayer(), 20, ClipContext.Fluid.NONE));
        Vec3 vec3 = Vec3.atBottomCenterOf(hitResult.getBlockPos().above());
        ShadowGate shadowGate = EntityInit.SHADOW_GATE.get().create(context.getLevel());
        if (shadowGate != null) {
            PortalInfo info = new PortalInfo(activePortals, vec3);
            this.portalInfo.put(shadowGate.getId(), info);
            context.getLevel().addFreshEntity(shadowGate);
            shadowGate.setPos(vec3.x(), vec3.y(), vec3.z());
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Level level = context.getLevel();
        if (!this.portalInfo.isEmpty()) {
            for (var entry : this.portalInfo.entrySet()) {
                ShadowGate shadowGate = (ShadowGate) level.getEntity(entry.getKey());
                if (shadowGate != null) {
                    List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, shadowGate.getBoundingBox());
                    List<LivingEntity> teleportList = new ObjectArrayList<>();
                    for (LivingEntity entity : entities) {
                        if (context.getSkillHandler().hasSkill(SkillInit.OPEN_INVITATION.value())) {
                            teleportList.add(entity);
                        } else if (entity.getUUID().equals(context.getPlayer().getUUID())) {
                            teleportList.add(entity);
                        }
                    }

                    ShadowGate adjacentGate = this.getAdjacentGate(shadowGate, level);
                    if (adjacentGate != null) {
                        for (LivingEntity entity : teleportList) {
                            if (!shadowGate.isOnCooldown(entity)) {
                                Vec3 position = adjacentGate.position();
                                adjacentGate.addCooldown(entity, 10);
                                entity.teleportTo(position.x, position.y, position.z);
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
        this.portalInfo.forEach((integer, portalInfo) -> {
            Entity entity = context.getLevel().getEntity(integer);
            if (entity != null) entity.discard();
        });
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
        super.load(nbt);
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
