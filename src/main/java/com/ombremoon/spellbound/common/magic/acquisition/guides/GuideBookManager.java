package com.ombremoon.spellbound.common.magic.acquisition.guides;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import java.util.*;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class GuideBookManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = Constants.LOG;
    private static final Gson GSON = new GsonBuilder().create();
    private static Map<ResourceLocation, List<GuideBookPage>> BOOKS = Map.of();
    private final HolderLookup.Provider registries;

    public GuideBookManager(HolderLookup.Provider registries) {
        super(GSON, "guide_books");
        this.registries = registries;
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> map = new HashMap();

        scanDirectory(resourceManager, "standard", map);
        scanDirectory(resourceManager, "ruin", map);
        scanDirectory(resourceManager, "divine", map);
        scanDirectory(resourceManager, "deception", map);
        scanDirectory(resourceManager, "summon", map);
        scanDirectory(resourceManager, "transfiguration", map);

        return map;
    }

    private void scanDirectory(ResourceManager manager, String name, Map<ResourceLocation, JsonElement> map) {
        scanDirectory(manager, "guide_books/" + name, GSON, map);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, List<GuideBookPage>> pages = new HashMap<>();
        object.forEach((location, json) -> {
            try {
                GuideBookPage page = GuideBookPage.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                if (page == null) {
                    LOGGER.debug("Skipping loading guide page {} as its conditions were not met", location);
                    return;
                }
                pages.computeIfAbsent(page.id(), k -> new ArrayList<>());
                pages.get(page.id()).add(page);
            } catch (Exception e) {
                LOGGER.error("Parsing error loading custom guide book page {}: {}", location, e.getMessage());
            }
        });

        for (ResourceLocation id : pages.keySet()) {
            BOOKS.put(id, sortPages(pages.get(id)));
        }
    }

    private List<GuideBookPage> sortPages(List<GuideBookPage> book) {
        List<GuideBookPage> result = new ArrayList<>();

        for (GuideBookPage page : book) {
            int index = -1;
            if (page.insertAfter() == null) {
                result.addFirst(page);
                continue;
            }

            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).id().equals(page.insertAfter())) {
                    index = i + 1;
                    break;
                }
            }

            if (index == -1) result.add(page);
            else result.add(index, page);
        }

        return result;
    }

    public static List<GuideBookPage> getBook(ResourceLocation id) {
        return BOOKS.get(id);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        GuideBookManager manager = new GuideBookManager(event.getRegistryAccess());
        event.addListener(manager);
    }
}
