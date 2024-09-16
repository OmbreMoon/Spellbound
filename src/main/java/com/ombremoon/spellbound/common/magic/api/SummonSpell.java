package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public abstract class SummonSpell extends AnimatedSpell {

    public SummonSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        if (context.getLevel() instanceof ServerLevel level) {
            Player player = context.getPlayer();
            SpellHandler handler = player.getData(DataInit.SPELL_HANDLER);
            for (int mob : handler.getSummonsForRemoval(player.tickCount)) {
                if (level.getEntity(mob) != null) level.getEntity(mob).kill();
            }
            handler.save(player);
        }
    }

    protected <T extends LivingEntity> void addMobs(SpellContext context, EntityType<T> summon, int mobCount) {
        if (context.getLevel() instanceof ServerLevel level) {
            Player player = context.getPlayer();
            SpellHandler handler = context.getPlayer().getData(DataInit.SPELL_HANDLER);

            Set<Integer> summonedMobs = new HashSet<>();
            Vec3 spawnPos = getSpawnPos(player);

            for (int i = 0; i < mobCount; i++) {
                T mob = summon.create(level);
                mob.setData(DataInit.OWNER_UUID, context.getPlayer().getUUID().toString());
                mob.teleportTo(spawnPos.x, spawnPos.y, spawnPos.z);
                level.addFreshEntity(mob);
                summonedMobs.add(mob.getId());
            }

            handler.addSummons(player.tickCount + getDuration(), summonedMobs);
            handler.save(player);
        }
    }

    private Vec3 getSpawnPos(Player player) {
        double rot = Math.toRadians(player.getYHeadRot());

        return new Vec3(
                player.getX() - 5.0 *  Math.sin(rot),
                player.getY(),
                player.getZ() + 5.0 * Math.cos(rot));
    }
}
