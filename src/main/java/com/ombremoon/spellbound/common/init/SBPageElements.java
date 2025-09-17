package com.ombremoon.spellbound.common.init;

import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.*;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class SBPageElements {
    public static final ResourceKey<Registry<MapCodec<? extends PageElement>>> PAGE_ELEMENT_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("page_element"));
    public static final Registry<MapCodec<? extends PageElement>> REGISTRY = new RegistryBuilder<>(PAGE_ELEMENT_REGISTRY_KEY).sync(true).create();
    public static final DeferredRegister<MapCodec<? extends PageElement>> PAGE_ELEMENTS = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Supplier<MapCodec<? extends PageElement>> TEXT = PAGE_ELEMENTS.register("text", () -> GuideText.CODEC);
    public static final Supplier<MapCodec<? extends PageElement>> IMAGE = PAGE_ELEMENTS.register("image", () -> GuideImage.CODEC);
    public static final Supplier<MapCodec<? extends PageElement>> ENTITY_RENDERER = PAGE_ELEMENTS.register("entity_renderer", () -> GuideEntityRenderer.CODEC);
    public static final Supplier<MapCodec<? extends PageElement>> ITEM = PAGE_ELEMENTS.register("item", () -> GuideItem.CODEC);
    public static final Supplier<MapCodec<? extends PageElement>> RECIPE = PAGE_ELEMENTS.register("recipe", () -> GuideRecipe.CODEC);
    public static final Supplier<MapCodec<? extends PageElement>> ITEM_LIST = PAGE_ELEMENTS.register("item_list", () -> GuideItemList.CODEC);

    public static void register(IEventBus eventBus) {
        PAGE_ELEMENTS.register(eventBus);
    }
}
