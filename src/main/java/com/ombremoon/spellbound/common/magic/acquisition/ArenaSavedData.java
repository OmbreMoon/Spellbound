package com.ombremoon.spellbound.common.magic.acquisition;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.UUID;

public class ArenaSavedData extends SavedData {
    private final Map<Integer, UUID> arenaMap = new Int2ObjectOpenHashMap<>();
    private int arenaId;

    public static ArenaSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(ArenaSavedData::create, ArenaSavedData::load), "_dynamic_dimension");
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
        ResourceLocation dimension = CommonClass.customLocation(uuid.toString());
        return ResourceKey.create(Registries.DIMENSION, dimension);
    }

    public boolean leftArena(Level level) {
        return Constants.MOD_ID.equals(level.dimension().location().getNamespace());
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
    }

    public static ArenaSavedData load(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
        ArenaSavedData data = create();
        data.load(nbt);
        return data;
    }
}
