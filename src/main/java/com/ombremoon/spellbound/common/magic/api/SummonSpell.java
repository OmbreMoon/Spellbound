package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.events.ChangeTargetEvent;
import com.ombremoon.spellbound.common.magic.events.PlayerDamageEvent;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.*;

public abstract class SummonSpell extends AnimatedSpell {
    private static final UUID DAMAGE_EVENT = UUID.fromString("79e708db-b942-4ca0-a6e8-2614f087881f");
    private static final UUID TARGETING_EVENT = UUID.fromString("d1d10463-e003-4dca-a6a0-bc5300d369d6");

    private int spellDuration = 0;
    private final Map<Integer, Set<Integer>> summons = new HashMap<>();

    public SummonSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.POST_DAMAGE, DAMAGE_EVENT, this::damageEvent);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.TARGETING_EVENT, TARGETING_EVENT, this::changeTargetEvent);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        if (context.getLevel() instanceof ServerLevel level) {
            Set<Integer> summonsForRemoval = this.summons.get(ticks);
            if (summonsForRemoval == null) return;

            for (int summonId : summonsForRemoval) {
                if (level.getEntity(summonId) != null) level.getEntity(summonId).discard();
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);

        for (Set<Integer> expiredSums : summons.values()) {
            for (int summonId : expiredSums) {
                Entity entity = context.getLevel().getEntity(summonId);
                if (context.getLevel().getEntity(summonId) != null)
                    context.getLevel().getEntity(summonId).discard();
            }
        }

        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.POST_DAMAGE, DAMAGE_EVENT);
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.TARGETING_EVENT, TARGETING_EVENT);
    }

    public Map<Integer, Set<Integer>> getSummons() {
        return this.summons;
    }

    protected <T extends Entity> Set<Integer> addMobs(SpellContext context, EntityType<T> summon, int mobCount, int lifeSpan) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        Set<Integer> summonedMobs = new HashSet<>();
        BlockPos blockPos = getSpawnPos(player, level);
        if (blockPos == null) return null;

        Vec3 spawnPos = blockPos.getCenter();
        for (int i = 0; i < mobCount; i++) {
            T mob = summon.create(level);
            mob.setData(DataInit.OWNER_UUID, context.getPlayer().getUUID().toString());
            mob.teleportTo(spawnPos.x, blockPos.getY(), spawnPos.z);
            level.addFreshEntity(mob);
            summonedMobs.add(mob.getId());
        }

        this.summons.put(ticks + lifeSpan, summonedMobs);
        this.spellDuration = ticks + lifeSpan;
        return summonedMobs;
    }

    private BlockPos getSpawnPos(Player player, Level level) {
        BlockHitResult blockHit = level.clip(setupRayTraceContext(player, 5d, ClipContext.Fluid.NONE));
        if (blockHit.getType() == HitResult.Type.MISS) return null;
        if (blockHit.getDirection() == Direction.DOWN) return null;

        return blockHit.getBlockPos().relative(blockHit.getDirection());
    }

    private void setSummonsTarget(Level level, Set<Integer> summons, LivingEntity target) {
        for (int mobId : summons) {
            if (level.getEntity(mobId) instanceof Monster monster) {
                monster.setData(DataInit.TARGET_ID, target.getId());
                monster.setTarget(target);
            }
        }
    }

    private void damageEvent(PlayerDamageEvent.Post damageEvent) {
        LivingDamageEvent.Post event = damageEvent.getDamageEvent();

        if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof LivingEntity newTarget) {
            SpellHandler handler = SpellUtil.getSpellHandler(event.getEntity());
            setSummonsTarget(player.level(), handler.getAllSummons(), newTarget);
        } else if (event.getSource().getEntity() instanceof Player player
                && !event.getEntity().getData(DataInit.OWNER_UUID).equals(player.getUUID().toString())) {
            SpellHandler handler = SpellUtil.getSpellHandler(event.getEntity());
            setSummonsTarget(player.level(), handler.getAllSummons(), event.getEntity());
        }
    }

    private void changeTargetEvent(ChangeTargetEvent targetEvent) {
        LivingChangeTargetEvent event = targetEvent.getTargetEvent();

        if (event.getNewAboutToBeSetTarget() == null) return;
        if (event.getEntity().getData(DataInit.OWNER_UUID).isEmpty()) return;

        int targetId = event.getEntity().getData(DataInit.TARGET_ID);

        if (targetId == 0) event.setNewAboutToBeSetTarget(null);
        else if (targetId != event.getNewAboutToBeSetTarget().getId())
            event.setNewAboutToBeSetTarget((LivingEntity) event.getEntity().level().getEntity(targetId));
    }
}
