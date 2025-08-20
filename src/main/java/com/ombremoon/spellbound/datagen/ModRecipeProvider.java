package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.common.init.SBBlocks;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.init.SBItems;
import com.ombremoon.spellbound.common.init.SBTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(pOutput, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SBItems.RITUAL_TALISMAN.get())
                .define('G', Items.GOLD_INGOT)
                .define('M', SBItems.MAGIC_ESSENCE.get())
                .pattern("GGG")
                .pattern(" M ")
                .pattern(" G ")
                .unlockedBy("has_magic_essence", has(SBItems.MAGIC_ESSENCE.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SBBlocks.TRANSFIGURATION_PEDESTAL.get())
                .define('W', Items.GREEN_WOOL)
                .define('L', ItemTags.LOGS)
                .define('S', ItemTags.WOODEN_SLABS)
                .define('M', SBItems.MAGIC_ESSENCE.get())
                .pattern("SWS")
                .pattern(" M ")
                .pattern("SLS")
                .unlockedBy("has_magic_essence", has(SBItems.MAGIC_ESSENCE.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SBBlocks.TRANSFIGURATION_DISPLAY.get())
                .define('S', ItemTags.STONE_BRICKS)
                .define('C', Items.COPPER_INGOT)
                .pattern(" S ")
                .pattern("CSC")
                .pattern("SSS")
                .unlockedBy("has_copper", has(Items.COPPER_INGOT))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SBItems.CHALK.get())
                .define('C', Items.CALCITE)
                .define('M', SBItems.MAGIC_ESSENCE.get())
                .pattern("C  ")
                .pattern(" M ")
                .pattern("  C")
                .unlockedBy("has_magic_essence", has(SBItems.MAGIC_ESSENCE.get()))
                .save(output);
       /* ShapedRecipeBuilder.shaped(RecipeCategory.MISC, talisman)
                .define('G', Items.GOLD_INGOT)
                .define('M', SBItems.MAGIC_ESSENCE.get())
                .pattern("GGG")
                .pattern("MGM")
                .pattern(" G ")
                .unlockedBy("has_magic_essence", has(SBItems.MAGIC_ESSENCE.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, talisman)
                .define('G', Items.GOLD_INGOT)
                .define('D', Items.DIAMOND)
                .define('M', SBItems.MAGIC_ESSENCE.get())
                .pattern("GGG")
                .pattern("MDM")
                .pattern(" G ")
                .unlockedBy("has_magic_essence", has(SBItems.MAGIC_ESSENCE.get()))
                .save(output);*/
    }
}
