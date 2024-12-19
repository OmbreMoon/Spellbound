package com.ombremoon.spellbound.common.content.world.dimension;

import com.ombremoon.spellbound.CommonClass;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

/**
 * @author Commoble
 * https://gist.github.com/Commoble/7db2ef25f94952a4d2e2b7e3d4be53e0
 */
// a Dimension is just a DimensionType + a ChunkGenerator
// we can define the dimension type in a json at data/yourmod/worldgen/dimension_type/your_dimension_type.json
// but we'll need to create instances of the chunk generator at runtime since there's no json folder for them
public class TestDimensionFactory {
    public static final ResourceKey<DimensionType> TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, CommonClass.customLocation("test"));

    public static LevelStem createDimension(MinecraftServer server, ResourceKey<LevelStem> key) {
        return new LevelStem(getDimensionTypeHolder(server), new TestChunkGenerator(server));
    }

    public static Holder<DimensionType> getDimensionTypeHolder(MinecraftServer server) {
        return server.registryAccess().holderOrThrow(TYPE_KEY);
    }
}
