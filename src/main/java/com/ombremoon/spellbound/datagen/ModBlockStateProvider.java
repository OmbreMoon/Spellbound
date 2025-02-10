package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Constants.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }

    private void blockItem(Supplier<Block> blockSupplier) {
        simpleBlockItem(blockSupplier.get(), new ModelFile.UncheckedModelFile("talesofskyrim:block/" + BuiltInRegistries.BLOCK.getKey(blockSupplier.get()).getPath()));
    }
    private void blockWithItem(Supplier<Block> blockSupplier) {
        simpleBlockWithItem(blockSupplier.get(), cubeAll(blockSupplier.get()));
    }

    protected BlockModelBuilder blockCubeTopModel(Block block) {
        String name = getName(block);
        return models().cubeBottomTop(name, modLoc("block/" + name + "_side"), modLoc("block/" + name + "_bottom"), modLoc("block/" + name + "_top"));
    }

    protected String getName(Block item) {
        return BuiltInRegistries.BLOCK.getKey(item).getPath();
    }

}
