package com.ombremoon.spellbound.common.init;

import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.RitualEffect;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.effects.CreateItem;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.effects.CreateSpellTome;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class SBRitualEffects {
    public static final ResourceKey<Registry<MapCodec<? extends RitualEffect>>> RITUAL_EFFECT_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("ritual_effect"));
    public static final Registry<MapCodec<? extends RitualEffect>> REGISTRY = new RegistryBuilder<>(RITUAL_EFFECT_REGISTRY_KEY).sync(true).create();
    public static final DeferredRegister<MapCodec<? extends RitualEffect>> RITUAL_EFFECTS = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Supplier<MapCodec<CreateItem>> CREATE_ITEM = RITUAL_EFFECTS.register("create_item", () -> CreateItem.CODEC);
    public static final Supplier<MapCodec<CreateSpellTome>> CREATE_SPELL_TOME = RITUAL_EFFECTS.register("create_spell_tome", () -> CreateSpellTome.CODEC);

    public static void register(IEventBus modEventBus) {
        RITUAL_EFFECTS.register(modEventBus);
    }
}
