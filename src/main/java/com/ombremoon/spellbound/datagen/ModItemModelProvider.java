package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SBItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Collection;
import java.util.function.Supplier;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        SBItems.SIMPLE_ITEM_LIST.stream().map(Supplier::get).forEach(this::tempItem);
        tempItem(SBItems.DEBUG.get());

    }

    private void registerItemModels(Collection<Supplier<? extends Item>> registryObjects) {
        registryObjects.stream().map(Supplier::get).forEach(this::simpleGeneratedModel);
    }

    protected ItemModelBuilder simpleGeneratedModel(Item item) {
        return simpleModel(item, mcLoc("item/generated"));
    }

    protected ItemModelBuilder simpleHandHeldModel(Item item) {
        return simpleModel(item, mcLoc("item/handheld"));
    }

    protected ItemModelBuilder tempHandHeldModel(Item item) {
        return tempModel(item, mcLoc("item/handheld"));
    }

    protected ItemModelBuilder simpleModel(Item item, ResourceLocation parent) {
        String name = getName(item);
        return singleTexture(name, parent, "layer0", modLoc("item/" + name));
    }

    protected ItemModelBuilder tempModel(Item item, ResourceLocation parent) {
        String name = getName(item);
        return singleTexture("temp", parent, "layer0", modLoc("item/" + name));
    }

    protected ItemModelBuilder tempItem(Item item) {
        return withExistingParent(BuiltInRegistries.ITEM.getKey(item).getPath(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated")).texture("layer0",
                CommonClass.customLocation("item/" + "temp_texture"));
    }

    protected String getName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    protected String getName(Block item) {
        return BuiltInRegistries.BLOCK.getKey(item).getPath();
    }

}

