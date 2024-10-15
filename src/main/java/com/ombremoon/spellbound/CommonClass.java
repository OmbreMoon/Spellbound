package com.ombremoon.spellbound;

import com.ombremoon.spellbound.common.init.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;

public class CommonClass {

    public static void init(IEventBus modEventBus) {
        SBItems.register(modEventBus);
        SBBlocks.register(modEventBus);
        SBEntities.register(modEventBus);
        SBSpells.register(modEventBus);
        SBEffects.register(modEventBus);
        SBData.register(modEventBus);
        SBStats.register(modEventBus);
        SBSkills.register(modEventBus);
        SBAttributes.register(modEventBus);
        SBDataTypes.register(modEventBus);
    }

    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    public static ResourceLocation customLocation(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
    }
}
