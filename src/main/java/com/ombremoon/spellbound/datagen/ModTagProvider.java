package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.common.content.world.multiblock.type.TransfigurationMultiblock;
import com.ombremoon.spellbound.common.init.SBBlocks;
import com.ombremoon.spellbound.common.init.SBItems;
import com.ombremoon.spellbound.common.init.SBTags;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModTagProvider {

    public static class Items extends TagsProvider<Item> {

        public Items(PackOutput p_256596_, CompletableFuture<HolderLookup.Provider> p_256513_, @Nullable ExistingFileHelper existingFileHelper) {
            super(p_256596_, Registries.ITEM, p_256513_, Constants.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            populateTag(ItemTags.DYEABLE, SBItems.CHALK);
        }

        public void populateTag(TagKey<Item> tag, Supplier<Item>... items){
            for (Supplier<Item> item : items) {
                tag(tag).add(BuiltInRegistries.ITEM.getResourceKey(item.get()).get());
            }
        }
    }

    public static class Blocks extends TagsProvider<Block> {

        public Blocks(PackOutput pGenerator, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
            super(pGenerator, Registries.BLOCK, provider, Constants.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            for (Block block : TransfigurationMultiblock.EXCLUDED_BLOCKS) {
                this.populateTag(SBTags.Blocks.RITUAL_INCOMPATIBLE, block);
            }
            this.populateTag(BlockTags.FLOWERS, SBBlocks.ARCANTHUS.get());
            this.populateTag(SBTags.Blocks.DIVINE_SHRINE, SBBlocks.JUNGLE_DIVINE_SHRINE.get(), SBBlocks.PLAINS_DIVINE_SHRINE.get(), SBBlocks.SANDSTONE_DIVINE_SHRINE.get());
        }

        public void populateTag(TagKey<Block> tag, Block... blocks){
            for (Block block : blocks) {
                tag(tag).add(BuiltInRegistries.BLOCK.getResourceKey(block).get());
            }
        }
    }
}
