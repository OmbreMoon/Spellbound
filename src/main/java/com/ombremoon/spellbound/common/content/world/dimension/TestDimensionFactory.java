package com.ombremoon.spellbound.common.content.world.dimension;

import com.mojang.serialization.DynamicOps;
import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SBChunkGenerators;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
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

    public static LevelStem createDimension(MinecraftServer server) {
        return new LevelStem(getDimensionTypeHolder(server), new TestChunkGenerator(server));
    }

    public static LevelStem createLevel(MinecraftServer server) {
        ServerLevel oldLevel = server.overworld();
        DynamicOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
        ChunkGenerator oldChunkGenerator = new TestChunkGenerator(server);
        ChunkGenerator newChunkGenerator = ChunkGenerator.CODEC.encodeStart(ops, oldChunkGenerator)
                .flatMap(nbt -> ChunkGenerator.CODEC.parse(ops, nbt))
                .getOrThrow(s -> new RuntimeException(String.format("Error copying dimension: {}", s)));
        Holder<DimensionType> typeHolder = oldLevel.dimensionTypeRegistration();
        return new LevelStem(typeHolder, newChunkGenerator);
    }

    public static Holder<DimensionType> getDimensionTypeHolder(MinecraftServer server) {
        return server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(TYPE_KEY);
    }
}
