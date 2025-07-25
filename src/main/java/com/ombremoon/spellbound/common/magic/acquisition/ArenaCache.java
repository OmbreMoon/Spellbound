package com.ombremoon.spellbound.common.magic.acquisition;

import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;

public class ArenaCache implements INBTSerializable<CompoundTag> {
    protected static final Logger LOGGER = Constants.LOG;
    private int arenaID;
    private BlockPos arenaPos;
    private ResourceKey<Level> arenaLevel;
    private boolean leftArena;

    public int getArenaID() {
        return this.arenaID;
    }

    public BlockPos getArenaPos() {
        return this.arenaPos;
    }

    public ResourceKey<Level> getArenaLevel() {
        return this.arenaLevel;
    }

    public void leaveArena() {
        this.leftArena = true;
    }

    public boolean leftArena() {
        return this.leftArena;
    }

    public void loadCache(int arenaID, BlockPos arenaPos, ResourceKey<Level> arenaLevel) {
        this.arenaID = arenaID;
        this.arenaPos = arenaPos;
        this.arenaLevel = arenaLevel;
    }

    public void clearCache() {
        this.arenaID = 0;
        this.arenaPos = null;
        this.arenaLevel = null;
        this.leftArena = false;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("PortalId", this.arenaID);

        if (this.arenaPos != null)
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, this.arenaPos).resultOrPartial(LOGGER::error).ifPresent(tag -> nbt.put("PortalPos", tag));

        if (this.arenaLevel != null)
            nbt.putString("ArenaLevel", this.arenaLevel.location().toString());

        nbt.putBoolean("LeftArena", this.leftArena);

        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        if (compoundTag.contains("ArenaCache", 10)) {
            CompoundTag nbt = compoundTag.getCompound("ArenaCache");
            if (nbt.contains("PortalId", 99))
                this.arenaID = nbt.getInt("PortalId");

            if (nbt.get("PortalPos") != null)
                BlockPos.CODEC.parse(NbtOps.INSTANCE, nbt.get("PortalPos")).resultOrPartial(LOGGER::error).ifPresent(blockPos -> this.arenaPos = blockPos);

            if (nbt.contains("ArenaLevel", 8))
                this.arenaLevel = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(nbt.getString("ArenaLevel")));

            if (nbt.contains("LeftArena", 99))
                this.leftArena = nbt.getBoolean("LeftArena");
        }
    }
}
