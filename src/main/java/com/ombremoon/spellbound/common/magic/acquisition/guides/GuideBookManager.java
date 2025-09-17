package com.ombremoon.spellbound.common.magic.acquisition.guides;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.main.CommonClass;
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
import org.jline.utils.Log;
import org.slf4j.Logger;

import java.util.*;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class GuideBookManager extends SimpleJsonResourceReloadListener {
    private static final ResourceLocation FIRST_PAGE = CommonClass.customLocation("first_page_dont_use");
    private static final Logger LOGGER = Constants.LOG;
    private static final Gson GSON = new GsonBuilder().create();
    private static Map<ResourceLocation, List<GuideBookPage>> BOOKS = new HashMap<>();

    public GuideBookManager() {
        super(GSON, "guide_books");
    }

    //Adds the different paths for the varying books to the scanner
    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> map = new HashMap();

        scanDirectory(resourceManager, "standard", map);
        scanDirectory(resourceManager, "ruin", map);
        scanDirectory(resourceManager, "divine", map);
        scanDirectory(resourceManager, "deception", map);
        scanDirectory(resourceManager, "summon", map);
        scanDirectory(resourceManager, "transfiguration", map);
        scanDirectory(resourceManager, "misc", map);

        return map;
    }

    /**
     * Scans directories for any valid page json files
     * @param manager The ResourceManager provided by {@link GuideBookManager#prepare(ResourceManager, ProfilerFiller)}
     * @param name The name of the folder to scan
     * @param map The map containing all pages scanned so far
     */
    private void scanDirectory(ResourceManager manager, String name, Map<ResourceLocation, JsonElement> map) {
        scanDirectory(manager, "guide_books/" + name, GSON, map);
    }

    /**
     * Processes all the read json files into GuideBookPages and calls the sort method
     * @param object ResourceLocation refers to the specific file and the JsonElement is the contents that have been read
     * @param resourceManager Honestly fuck knows what this is il be real
     * @param profiler Same as above cant lie
     */
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, List<Pair<ResourceLocation, GuideBookPage>>> pages = new HashMap<>();
        object.forEach((location, json) -> {
            try {
                GuideBookPage page = GuideBookPage.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                if (page == null) {
                    LOGGER.debug("Skipping loading guide page {} as its conditions were not met", location);
                    return;
                }
                pages.computeIfAbsent(page.id(), k -> new ArrayList<>());
                pages.get(page.id()).add(Pair.of(location, page));
            } catch (Exception e) {
                LOGGER.error("Parsing error loading custom guide book page {}: {}", location, e.getMessage());
            }
        });

        for (ResourceLocation id : pages.keySet()) {
            BOOKS.put(id, sortPages(pages.get(id)));
        }
    }

    /**
     * Sorts the pages so that each page goes in the appropriate order
     * @param book The current book being sorted, Each pair represents the id of the page and the page itself
     * @return A sorted list of pages
     */
    private List<GuideBookPage> sortPages(List<Pair<ResourceLocation, GuideBookPage>> book) {
        List<Pair<ResourceLocation, GuideBookPage>> result = new ArrayList<>();

        for (Pair<ResourceLocation, GuideBookPage> pair : book) {
            GuideBookPage page = pair.getSecond();
            int index = -1;
            if (page.insertAfter().equals(FIRST_PAGE)) {
                result.addFirst(pair);
                continue;
            }

            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).getFirst().equals(page.insertAfter())) {
                    index = i + 1;
                    break;
                }
            }

            if (index == -1) result.add(pair);
            else result.add(index, pair);
        }

        List<GuideBookPage> toRet = result.stream().map(Pair::getSecond).toList();
        return toRet;
    }

    /**
     * Gets a selected books pages
     * @param id The id of the book to get the pages of
     * @return The sorted pages of the given book id
     */
    public static List<GuideBookPage> getBook(ResourceLocation id) {
        return BOOKS.get(id);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        GuideBookManager manager = new GuideBookManager();
        event.addListener(manager);
    }
}
