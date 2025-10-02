package com.ombremoon.spellbound.common.magic.acquisition.bosses;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.common.init.SBBossFights;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Function;

public abstract class BossFightInstance<T extends BossFight, S extends BossFightInstance<T, S>> {
    public static final Codec<BossFightInstance<?, ?>> CODEC = SBBossFights.REGISTRY
            .byNameCodec()
            .dispatch(BossFightInstance::codec, Function.identity());

    protected final T bossFight;
    protected boolean defeatedBoss;
    private boolean initialized;

    public BossFightInstance(T bossFight) {
        this.bossFight = bossFight;
    }

    public abstract void initializeWinCondition(ServerLevel level, T bossFight);

    public abstract boolean winCondition(ServerLevel level, T bossFight);

    public abstract void endFight(ServerLevel level, T bossFight);

    public abstract MapCodec<S> codec();

    public abstract CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries);

    public abstract void load(CompoundTag nbt);

    public boolean isInitialized() {
        return this.initialized;
    }

    public void start(ServerLevel level) {
        this.initializeWinCondition(level, this.bossFight);
        this.initialized = true;
    }

    public void handleBossFightLogic(ServerLevel level) {
        this.tickFight(level, this.bossFight);
    }

    protected void tickFight(ServerLevel level, T bossFight) {
        if (!ArenaSavedData.isArenaEmpty(level) && this.winCondition(level, bossFight)) {
            this.endFight(level, bossFight);

            ArenaSavedData data = ArenaSavedData.get(level);
            data.endFight();
        }
    }

    public T getBossFight() {
        return this.bossFight;
    }
}
