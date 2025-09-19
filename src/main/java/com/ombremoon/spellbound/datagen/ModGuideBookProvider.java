package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.common.magic.acquisition.guides.GuideBookPage;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.ElementPosition;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.GuideText;
import com.ombremoon.spellbound.datagen.provider.GuideBookProvider;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModGuideBookProvider extends GuideBookProvider {
    public ModGuideBookProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<GuideBookPage> writer) {
//        var book = new GuideBookPage(CommonClass.customLocation("test"), CommonClass.customLocation("null"), List.of(), List.of(new GuideText("hi", new ElementPosition(3, 5))));
//        writer.accept(book);
    }
}
