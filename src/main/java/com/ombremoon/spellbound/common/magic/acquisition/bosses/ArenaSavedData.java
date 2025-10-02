package com.ombremoon.spellbound.common.magic.acquisition.bosses;

import com.mojang.serialization.Dynamic;
import com.ombremoon.spellbound.common.content.world.dimension.DynamicDimensionFactory;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;

public class ArenaSavedData extends SavedData {
    public static final Logger LOGGER = Constants.LOG;
    private final Map<Integer, UUID> arenaMap = new Int2ObjectOpenHashMap<>();
    private int arenaId;

    //For arena levels
    public boolean spawnedArena;
    private ResourceLocation spellLocation;
    private BossFightInstance<?, ?> currentBossFight;

    public static ArenaSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(ArenaSavedData::create, ArenaSavedData::load), "_dynamic_dimension");
    }

    private ArenaSavedData() {}

    private static ArenaSavedData create() {
        return new ArenaSavedData();
    }

    public int incrementId() {
        this.arenaId++;
        this.setDirty();
        return this.arenaId;
    }

    public int getCurrentId() {
        return this.arenaId;
    }

    public UUID getOrCreateUuid(MinecraftServer server, int arenaId) {
        if (!this.arenaMap.containsKey(arenaId)) {
            UUID uuid;
            ResourceLocation dimension;
            ResourceKey<Level> levelKey;
            do {
                uuid = UUID.randomUUID();
                dimension = CommonClass.customLocation(uuid.toString());
                levelKey = ResourceKey.create(Registries.DIMENSION, dimension);
            } while (server.levelKeys().contains(levelKey));
            this.arenaMap.put(arenaId, uuid);
            this.setDirty();
        }
        return this.arenaMap.get(arenaId);
    }

    public ResourceKey<Level> getOrCreateKey(MinecraftServer server, int arenaId) {
        UUID uuid = getOrCreateUuid(server, arenaId);
        ResourceLocation dimension = CommonClass.customLocation(uuid + "_arena");
        return ResourceKey.create(Registries.DIMENSION, dimension);
    }

    public static boolean isArena(Level level) {
        return level.dimension().location().getPath().endsWith("_arena");
    }

    public static boolean isArenaEmpty(ServerLevel level) {
        return isArena(level) && level.getPlayers(player -> !player.isSpectator()).isEmpty();
    }

    public void spawnArena(ServerLevel level) {
        DynamicDimensionFactory.spawnArena(level, this.spellLocation);
    }

    public void spawnInArena(ServerLevel level, Entity entity) {
        if (this.currentBossFight != null)
            DynamicDimensionFactory.spawnInArena(level, entity, this.currentBossFight.getBossFight());
    }

    public void handleBossFightLogic(ServerLevel level) {
        if (this.currentBossFight != null)
            this.currentBossFight.handleBossFightLogic(level);
    }

    public void endFight() {
        this.currentBossFight = null;
    }

    public void initializeArena(ServerLevel level, ResourceLocation spellLocation, BossFight bossFight) {
        this.spellLocation = spellLocation;
        this.currentBossFight = bossFight.createFight(level);
        this.setDirty();
    }

    public BossFightInstance<?, ?> getCurrentBossFight() {
        return this.currentBossFight;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag tagList = new ListTag();
        for (var entry : arenaMap.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("ArenaId", entry.getKey());
            entryTag.putUUID("ArenaUUID", entry.getValue());
            tagList.add(entryTag);
        }
        tag.put("Arenas", tagList);
        tag.putInt("CurrentArenaId", this.arenaId);

        tag.putBoolean("SpawnedArena", this.spawnedArena);
        if (this.spellLocation != null) {
            ResourceLocation.CODEC
                    .encodeStart(NbtOps.INSTANCE, this.spellLocation)
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(nbt -> tag.put("ArenaSpell", nbt));
        }

        if (this.currentBossFight != null) {
            BossFightInstance.CODEC
                    .encodeStart(NbtOps.INSTANCE, this.currentBossFight)
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(nbt -> {
                        tag.put("CurrentBossFight", nbt);
                        this.currentBossFight.save(tag, registries);
                    });
        }
        return tag;
    }

    public void load(CompoundTag nbt) {
        this.arenaMap.clear();
        final ListTag listTag = nbt.getList("Arenas", 10);
        for (int i = 0, l = listTag.size(); i < l; i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            int id = compoundTag.getInt("ArenaId");
            UUID uuid = compoundTag.getUUID("ArenaUUID");
            this.arenaMap.put(id, uuid);
        }
        this.arenaId = nbt.getInt("CurrentArenaId");

        this.spawnedArena = nbt.getBoolean("SpawnedArena");
        if (nbt.contains("ArenaSpell", 10)) {
            ResourceLocation.CODEC
                    .parse(new Dynamic<>(NbtOps.INSTANCE, nbt.get("ArenaSpell")))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(location -> this.spellLocation = location);
        }

        if (nbt.contains("CurrentBossFight", 10)) {
            BossFightInstance.CODEC
                    .parse(new Dynamic<>(NbtOps.INSTANCE, nbt.get("CurrentBossFight")))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(bossFightInstance -> {
                        this.currentBossFight = bossFightInstance;
                        bossFightInstance.load(nbt);
                    });
        }
    }

    public static ArenaSavedData load(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
        ArenaSavedData data = create();
        data.load(nbt);
        return data;
    }
}
