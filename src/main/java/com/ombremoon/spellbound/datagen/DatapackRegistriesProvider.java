package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.common.init.SBDamageTypes;
import com.ombremoon.spellbound.common.init.SBRituals;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.main.Keys;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DatapackRegistriesProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Keys.RITUAL, SBRituals::bootstrap)
            .add(Registries.DAMAGE_TYPE, SBDamageTypes::bootstrap);

    public DatapackRegistriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Constants.MOD_ID));
    }
}
