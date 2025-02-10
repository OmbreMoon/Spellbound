package com.ombremoon.spellbound.common.content.world.dimension;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import net.commoble.infiniverse.api.InfiniverseAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DynamicDimensionFactory {

    public static ServerLevel createDimension(MinecraftServer server, ResourceKey<Level> levelKey) {
        return InfiniverseAPI.get().getOrCreateLevel(server, levelKey, () -> createLevel(server));
    }

    private static LevelStem createLevel(MinecraftServer server) {
        ChunkGenerator oldChunkGenerator = new EmptyChunkGenerator(server);
        Holder<DimensionType> typeHolder = server.overworld().dimensionTypeRegistration();
        return new LevelStem(typeHolder, oldChunkGenerator);
    }

    public static void spawnInArena(Entity entity, ServerLevel level, ResourceLocation spell, boolean spawnArena) {
        BlockPos blockPos = new BlockPos(0, 64, 0);
        level.getChunkAt(blockPos);
        spawnArena(level, blockPos, spell, spawnArena);

        Vec3 targetVec = Vec3.atBottomCenterOf(blockPos.offset(13, 16, 5));
        sendToDimension(entity, level, targetVec);
    }

    private static void spawnArena(ServerLevel level, BlockPos origin, ResourceLocation spell, boolean spawnArena) {
        StructureTemplate template = getTemplate(level, spell.getPath());
        if (template == null)
            return;

        RandomSource rand = RandomSource.create(spell.hashCode() + level.dimension().location().hashCode());
        BoundingBox boundingBox = BoundingBox.fromCorners(origin.subtract(template.getSize()), origin.offset(template.getSize()));
        StructurePlaceSettings placement = new StructurePlaceSettings()
                .setRandom(rand).setBoundingBox(boundingBox)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);

        if (spawnArena)
            template.placeInWorld(level, origin, origin, placement, rand, Block.UPDATE_ALL);
    }

    private static void sendToDimension(Entity entity, ServerLevel level, Vec3 targetVec) {
        level.getChunk(new BlockPos(Mth.floor(targetVec.x), Mth.floor(targetVec.y), Mth.floor(targetVec.z)));
        float f = Direction.WEST.toYRot();
        var transition = new DimensionTransition(level, targetVec, entity.getDeltaMovement(), f, entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
        entity.changeDimension(transition);
        entity.portalCooldown = 20;
    }

    private static StructureTemplate getTemplate(Level level, String spell) {
        String name = "arena/" + spell;
        ResourceLocation template = CommonClass.customLocation(name);
        return getTemplate(level, template);
    }

    @Nullable
    public static StructureTemplate getTemplate(Level level, ResourceLocation location) {
        if (level.getServer() == null)
            return null;

        StructureTemplateManager templateManager = level.getServer().getStructureManager();
        Optional<StructureTemplate> template = templateManager.get(location);
        if (template.isEmpty())
            Constants.LOG.warn("Failed to load arena template for {}", location);

        return template.orElse(null);
    }
}
