package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    private static final String CHEST = "chests";
    private static final String VILLAGE = "village";
    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Constants.MOD_ID);
    }

    @Override
    protected void start() {

    }

/*    protected void addToEntityLootTable(String modifierName, EntityType<?> entityType, float dropChance, float lootMultiplier, Item item) {
        add(modifierName, new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(entityType.getDefaultLootTable()).build(),
                LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(dropChance, lootMultiplier).build()
        }, item));
    }

    protected void addToStructureLootTable(String modifierName, ResourceLocation resourceLocation, Item item, float probabilityChance) {
        add(modifierName, new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(resourceLocation).build(),
                LootItemRandomChanceCondition.randomChance(probabilityChance).build()
        }, item));
    }*/
}
