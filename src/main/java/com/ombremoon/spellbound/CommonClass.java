package com.ombremoon.spellbound;

import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.mixin.DuckRangedAttribute;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        SBData.register(modEventBus);
        SBStats.register(modEventBus);
        SBSkills.register(modEventBus);
        SBAttributes.register(modEventBus);
        SBDataTypes.register(modEventBus);
        SBTriggers.register(modEventBus);
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
