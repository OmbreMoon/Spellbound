package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.common.content.world.features.ArcanthusConfig;
import com.ombremoon.spellbound.common.content.world.features.ArcanthusFeature;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.main.Spellbound;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.awt.*;
import java.util.function.Supplier;

public class SBFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Constants.MOD_ID);

    public static final Supplier<ArcanthusFeature> ARCANTHUS = FEATURES.register("arcanthus_patch", () -> new ArcanthusFeature(ArcanthusConfig.CODEC));

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}
