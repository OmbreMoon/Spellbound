package com.ombremoon.spellbound.common.content.world.dimension;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import com.ombremoon.spellbound.Constants;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

/**
 * @author Commoble
 * https://gist.github.com/Commoble/7db2ef25f94952a4d2e2b7e3d4be53e0
 */
public class DynamicDimensionFactory {
    private static final RegistrationInfo DEFAULT_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());

    public static void spawnInArena(Entity entity, ServerLevel level) {
        BlockPos blockPos = new BlockPos(0, 64, 0);
        Vec3 targetVec = Vec3.atBottomCenterOf(blockPos);
        level.getChunkAt(blockPos);
        //SPAWN ARENA STRUCTURE
        sendToDimension(entity, level, targetVec);
    }

    private static void sendToDimension(Entity entity, ServerLevel level, Vec3 targetVec) {
        level.getChunkAt(new BlockPos(Mth.floor(targetVec.x), Mth.floor(targetVec.y), Mth.floor(targetVec.z)));
        float f = Direction.WEST.toYRot();
        var transition = new DimensionTransition(level, targetVec, entity.getDeltaMovement(), f, entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
        entity.changeDimension(transition);
//          entity.portalCooldown = something
    }

    public static ServerLevel getOrCreateWorld(MinecraftServer server, ResourceKey<Level> level, BiFunction<MinecraftServer, ResourceKey<LevelStem>, LevelStem> dimensionFactory) {
        @SuppressWarnings("deprecation")
        Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();
        ServerLevel serverLevel = map.get(level);
        return serverLevel == null ? createAndRegisterLevelAndDimension(server, map, level, dimensionFactory) : serverLevel;
    }

    @SuppressWarnings("deprecation")
    private static ServerLevel createAndRegisterLevelAndDimension(MinecraftServer server, Map<ResourceKey<Level>, ServerLevel> map, ResourceKey<Level> level, BiFunction<MinecraftServer, ResourceKey<LevelStem>, LevelStem> dimensionFactory) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registries.LEVEL_STEM, level.location());
        LevelStem dimension = dimensionFactory.apply(server, dimensionKey);

        ChunkProgressListener chunkListener = server.progressListenerFactory.create(11);
        Executor executor = server.executor;
        LevelStorageSource.LevelStorageAccess levelSave = server.storageSource;

        final WorldData worldData = server.getWorldData();
        final DerivedLevelData data = new DerivedLevelData(worldData, worldData.overworldData());

        Registry<LevelStem> dimensionRegistry = server.registries().compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
        if (dimensionRegistry instanceof MappedRegistry<LevelStem> mappedRegistry) {
            safelyUnfreezeRegistry(mappedRegistry, dimensionKey, dimension);
        } else {
            throw new IllegalStateException(String.format("Unable to register dimension %s -- dimension registry not writable", dimensionKey.location()));
        }

        ServerLevel newLevel = new ServerLevel(
                server,
                executor,
                levelSave,
                data,
                level,
                dimension,
                chunkListener,
                worldData.isDebugWorld(),
                BiomeManager.obfuscateSeed(worldData.worldGenOptions().seed()),
                ImmutableList.of(),
                false,
                null
        );

        overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newLevel.getWorldBorder()));
        map.put(level, newLevel);
        server.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(newLevel));
        return newLevel;
    }

    private static void safelyUnfreezeRegistry(MappedRegistry<LevelStem> registry, ResourceKey<LevelStem> dimensionKey, LevelStem dimension) {
        if (registry.frozen)
            registry.frozen = false;

        registry.register(dimensionKey, dimension, RegistrationInfo.BUILT_IN);
        registry.frozen = true;
    }
}
