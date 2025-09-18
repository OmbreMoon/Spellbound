package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public interface SBPlacedFeatures {
    ResourceKey<PlacedFeature> FIRE_GEODE = register("fire_geode");
    ResourceKey<PlacedFeature> ICE_GEODE = register("ice_geode");
    ResourceKey<PlacedFeature> SHOCK_GEODE = register("shock_geode");

    private static ResourceKey<PlacedFeature> register(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, CommonClass.customLocation(name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>>  feature, PlacementModifier... placement) {
        context.register(key, new PlacedFeature(feature, List.of(placement)));
    }

    static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var holderGetter = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> fireGeode = holderGetter.getOrThrow(SBConfiguredFeatures.FIRE_GEODE);
        register(
                context,
                FIRE_GEODE,
                fireGeode,
                RarityFilter.onAverageOnceEvery(24),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(30)),
                BiomeFilter.biome()
        );
        Holder<ConfiguredFeature<?, ?>> iceGeode = holderGetter.getOrThrow(SBConfiguredFeatures.ICE_GEODE);
        register(
                context,
                ICE_GEODE,
                iceGeode,
                RarityFilter.onAverageOnceEvery(24),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(30)),
                BiomeFilter.biome()
        );
        Holder<ConfiguredFeature<?, ?>> shockGeode = holderGetter.getOrThrow(SBConfiguredFeatures.SHOCK_GEODE);
        register(
                context,
                SHOCK_GEODE,
                shockGeode,
                RarityFilter.onAverageOnceEvery(24),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(64), VerticalAnchor.absolute(128)),
                BiomeFilter.biome()
        );
    }
}
