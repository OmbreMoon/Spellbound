package com.ombremoon.spellbound.main;

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
        SBBlockEntities.register(modEventBus);
        SBArmorMaterials.register(modEventBus);
        SBParticles.register(modEventBus);
        SBData.register(modEventBus);
        SBStats.register(modEventBus);
        SBSkills.register(modEventBus);
        SBAttributes.register(modEventBus);
        SBDataTypes.register(modEventBus);
        SBTriggers.register(modEventBus);
        SBChunkGenerators.register(modEventBus);
        SBFeatures.register(modEventBus);
        SBMultiblockSerializers.register(modEventBus);
        SBRitualEffects.register(modEventBus);
//        fixAttributes();
    }

    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    private static void fixAttributes() {
    }

    public static ResourceLocation customLocation(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
    }
}
