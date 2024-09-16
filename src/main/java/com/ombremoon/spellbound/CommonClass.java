package com.ombremoon.spellbound;

import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.ItemInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.init.StatInit;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;

public class CommonClass {

    public static void init(IEventBus modEventBus) {
        ItemInit.register(modEventBus);
        SpellInit.register(modEventBus);
        DataInit.register(modEventBus);
        StatInit.register(modEventBus);
    }

    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    public static ResourceLocation customLocation(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
    }
}
