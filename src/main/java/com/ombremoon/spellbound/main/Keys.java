package com.ombremoon.spellbound.main;

import com.ombremoon.spellbound.common.content.world.multiblock.Multiblock;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.TransfigurationRitual;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class Keys {
    public static final ResourceKey<Registry<TransfigurationRitual>> RITUAL = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("transfiguration_ritual"));
    public static final ResourceKey<Registry<Multiblock>> MULTIBLOCKS = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("multiblocks"));

}
