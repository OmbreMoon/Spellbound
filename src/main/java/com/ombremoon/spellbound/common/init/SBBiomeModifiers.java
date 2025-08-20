package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public interface SBBiomeModifiers {
    ResourceKey<BiomeModifier> ICE_GEODE = register("ice_geode");

    private static ResourceKey<BiomeModifier> register(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, CommonClass.customLocation(name));
    }

    static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var featureGetter = context.lookup(Registries.PLACED_FEATURE);
        var biomeGetter = context.lookup(Registries.BIOME);

        context.register(
                ICE_GEODE,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomeGetter.getOrThrow(Tags.Biomes.IS_COLD_OVERWORLD),
                        HolderSet.direct(featureGetter.getOrThrow(SBPlacedFeatures.ICE_GEODE)),
                        GenerationStep.Decoration.LOCAL_MODIFICATIONS
                )
        );
    }
}
