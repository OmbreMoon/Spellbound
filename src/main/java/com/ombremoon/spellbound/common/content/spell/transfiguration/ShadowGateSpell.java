package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.common.content.entity.ShadowGate;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Map;

public class ShadowGateSpell extends AnimatedSpell {
    private static Builder<AnimatedSpell> createShadowGateBuilder() {
        return createSimpleSpellBuilder().castTime(20).duration(1200).castCondition(context -> {
            BlockHitResult hitResult = context.getLevel().clip(setupRayTraceContext(context.getPlayer(), 5d, ClipContext.Fluid.NONE));
            if (hitResult.getType() == HitResult.Type.MISS) return false;

            BlockPos blockPos = hitResult.getBlockPos();
            if (context.getSkillHandler().hasSkill(SkillInit.DARKNESS_PREVAILS.value())) return true;
            return context.getLevel().getBrightness(LightLayer.SKY, blockPos) <= 3;
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
        if (activePortals == 2 || (context.getSkillHandler().hasSkill(SkillInit.DUAL_DESTINATION.value()) && activePortals == 3)) {
            //Play fail animation
            return;
        }

        BlockHitResult hitResult = context.getLevel().clip(setupRayTraceContext(context.getPlayer(), 5d, ClipContext.Fluid.NONE));
        BlockPos blockPos = hitResult.getBlockPos();
        ShadowGate shadowGate = EntityInit.SHADOW_GATE.get().create(context.getLevel());
        if (shadowGate != null) {
            PortalInfo info = new PortalInfo(activePortals, blockPos);
            this.portalInfo.put(shadowGate.getId(), info);
            //ADD PORTAL
            //SET POS
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        ListTag listTag = new ListTag();
        for (var entry : this.portalInfo.entrySet()) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("PortalEntityId", entry.getKey());
            nbt.putInt("PortalId", entry.getValue().id());

            BlockPos blockPos = entry.getValue().blockPos();
            nbt.putIntArray("PortalPosition", List.of(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
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
                this.portalInfo.put(entityId, new PortalInfo(portalId, new BlockPos(posArray[0], posArray[1], posArray[2])));
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

    private record PortalInfo(int id, BlockPos blockPos) {}
}
