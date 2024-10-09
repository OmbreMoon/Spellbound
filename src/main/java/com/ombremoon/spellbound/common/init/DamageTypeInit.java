package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public interface DamageTypeInit {
    ResourceKey<DamageType> DISEASE = register("disease");
    ResourceKey<DamageType> RUIN_FIRE = register("ruin_fire");

    private static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, CommonClass.customLocation(name));
    }

    static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(DISEASE, new DamageType("disease", 0.1F));
        context.register(RUIN_FIRE, new DamageType("ruin_fire", 0.1F));
    }
}
