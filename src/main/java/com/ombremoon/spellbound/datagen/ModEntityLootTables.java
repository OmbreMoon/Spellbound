package com.ombremoon.spellbound.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.stream.Stream;

public class ModEntityLootTables extends EntityLootSubProvider {

    protected ModEntityLootTables(HolderLookup.Provider registries) {
        super(FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    public void generate() {
    }

    private void multiDrops(EntityType<?> type, LootEntry... entries) {
        LootPool.Builder pool = LootPool.lootPool();
        pool.setRolls(ConstantValue.exactly(1));
        for (LootEntry entry : entries) {
            pool.add(LootItem.lootTableItem(entry.item()).apply(SetItemCountFunction.setCount(entry.numberProvider())));
        }
        this.add(type, LootTable.lootTable().withPool(pool));
    }

    private void dropRange(EntityType<?> entityType, Item item, float min, float max) {
        LootTable.Builder builder = LootTable.lootTable();
        builder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))));
        add(entityType, builder);
    }

    private void dropSingle(EntityType<?> entityType, Item item) {
        dropSetAmount(entityType, item, 1);
    }

    private void dropSetAmount(EntityType<?> entityType, Item item, float amount) {
        LootTable.Builder builder = LootTable.lootTable();
        builder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount)))));
        add(entityType, builder);
    }

    private void multiDropsWithChance(EntityType<?> type, LootEntry... entries) {
        LootPool.Builder pool = LootPool.lootPool();
        pool.setRolls(ConstantValue.exactly(1));
        for (LootEntry entry : entries) {
            pool.add(LootItem.lootTableItem(entry.item()).apply(SetItemCountFunction.setCount(entry.numberProvider()))).when(LootItemRandomChanceCondition.randomChance(entry.chance()));
        }
        this.add(type, LootTable.lootTable().withPool(pool));
    }

    private void dropRangeWithChance(EntityType<?> entityType, Item item, float min, float max, float chance) {
        LootTable.Builder builder = LootTable.lootTable();
        builder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))).when(LootItemRandomChanceCondition.randomChance(chance)));
        add(entityType, builder);
    }

    private void dropSingleWithChance(EntityType<?> entityType, Item item, float chance) {
        dropSetAmountWithChance(entityType, item, 1, chance);
    }

    private void dropSetAmountWithChance(EntityType<?> entityType, Item item, float amount, float chance) {
        LootTable.Builder builder = LootTable.lootTable();
        builder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount)))).when(LootItemRandomChanceCondition.randomChance(chance)));
        add(entityType, builder);
    }

    @Override
    protected boolean canHaveLootTable(EntityType<?> pEntityType) {
        return false;
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of();
    }

    record LootEntry(Item item, NumberProvider numberProvider, float chance) {}
}
