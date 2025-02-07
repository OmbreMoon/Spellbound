package com.ombremoon.spellbound.common.content.world.dimension;

import net.commoble.infiniverse.api.InfiniverseAPI;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class TestDimensionFactory {

    public static ServerLevel createDimension(MinecraftServer server, ResourceKey<Level> levelKey) {
        return InfiniverseAPI.get().getOrCreateLevel(server, levelKey, () -> createLevel(server));
    }

    public static LevelStem createLevel(MinecraftServer server) {
        ChunkGenerator oldChunkGenerator = new TestChunkGenerator(server);
        Holder<DimensionType> typeHolder = server.overworld().dimensionTypeRegistration();
        return new LevelStem(typeHolder, oldChunkGenerator);
    }
}
