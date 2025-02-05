package com.ombremoon.spellbound.common.content.world.dimension;

import net.commoble.infiniverse.api.InfiniverseAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class DynamicDimensionFactory {
    static final InfiniverseAPI API = InfiniverseAPI.get();

    public static void spawnInArena(Entity entity, ServerLevel level) {
        BlockPos blockPos = new BlockPos(0, 64, 0);
        Vec3 targetVec = Vec3.atBottomCenterOf(blockPos);
        level.getChunkAt(blockPos);
        //SPAWN ARENA STRUCTURE
        sendToDimension(entity, level, targetVec);
    }

    private static void sendToDimension(Entity entity, ServerLevel level, Vec3 targetVec) {
        level.getChunk(new BlockPos(Mth.floor(targetVec.x), Mth.floor(targetVec.y), Mth.floor(targetVec.z)));
        float f = Direction.WEST.toYRot();
        var transition = new DimensionTransition(level, targetVec, entity.getDeltaMovement(), f, entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
        entity.changeDimension(transition);
//          entity.portalCooldown = something
    }

    public static ServerLevel getOrCreateWorld(MinecraftServer server, ResourceKey<Level> level, Function<MinecraftServer, LevelStem> dimensionFactory) {
        return API.getOrCreateLevel(server, level, () -> dimensionFactory.apply(server));
    }
}
