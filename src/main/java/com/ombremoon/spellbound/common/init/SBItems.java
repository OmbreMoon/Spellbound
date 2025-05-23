package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.common.content.item.*;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SBItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
    public static final List<Supplier<? extends Item>> SIMPLE_ITEM_LIST = new ArrayList<>();
    public static final List<Supplier<? extends Item>> BLOCK_ITEM_LIST = new ArrayList<>();

    public static final Supplier<Item> DEBUG = ITEMS.register("debug", () -> new DebugItem(getItemProperties()));
    public static final Supplier<Item> SOUL_SHARD = registerSimpleItem("soul_shard");
    public static final Supplier<Item> SMOLDERING_SHARD = registerSimpleItem("smoldering_shard");
    public static final Supplier<Item> FROZEN_SHARD = registerSimpleItem("frozen_shard");
    public static final Supplier<Item> STORM_SHARD = registerSimpleItem("storm_shard");
    public static final Supplier<Item> HOLY_SHARD = registerSimpleItem("holy_shard");
    public static final Supplier<Item> FOOL_SHARD = registerSimpleItem("fool_shard");

    public static final Supplier<Item> TRANSFIGURATION_STAFF = registerCatalystItem("transfiguration_staff", SpellPath.TRANSFIGURATION);

    public static final Supplier<Item> TRANSFIGURER_HELMET = registerArmorItem("transfigurer_helmet", SBArmorMaterials.TRANSFIGURER, ArmorItem.Type.HELMET);
    public static final Supplier<Item> TRANSFIGURER_CHESTPLATE = registerArmorItem("transfigurer_chestplate", SBArmorMaterials.TRANSFIGURER, ArmorItem.Type.CHESTPLATE);
    public static final Supplier<Item> TRANSFIGURER_LEGGINGS = registerArmorItem("transfigurer_leggings", SBArmorMaterials.TRANSFIGURER, ArmorItem.Type.LEGGINGS);
    public static final Supplier<Item> TRANSFIGURER_BOOTS = registerArmorItem("transfigurer_boots", SBArmorMaterials.TRANSFIGURER, ArmorItem.Type.BOOTS);

    public static final Supplier<Item> SPELL_TOME = ITEMS.register("spell_tome", () -> new SpellTomeItem(getItemProperties()));
    public static final Supplier<Item> MANA_TEAR = registerItem("mana_tear", () -> new ManaTearItem(getItemProperties()));

    public static final Supplier<CreativeModeTab> SPELL_TAB = CREATIVE_MODE_TABS.register("spell_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP,0)
            .displayItems(
                    (itemDisplayParameters,output)-> {
                        ITEMS.getEntries().forEach((registryObject)-> {
                            if (registryObject != SPELL_TOME) output.accept(new ItemStack(registryObject.get()));
                        });
                        SBSpells.SPELL_TYPES.getEntries().forEach((registryObject) -> {
                            output.accept(SpellTomeItem.createWithSpell(registryObject.get()));
                        });
                    }).title(Component.translatable("itemGroup.spellbound"))
            .build());

    public static Supplier<Item> registerSpellTome(String name, Supplier<? extends SpellType<?>> spellType) {
        Supplier<Item> item = ITEMS.register(name, () -> new SpellTomeItem(getItemProperties()));
        SIMPLE_ITEM_LIST.add(item);
        return item;
    }

    public static Supplier<Item> registerSimpleItem(String name) {
        Supplier<Item> item = ITEMS.register(name, () -> new Item(getItemProperties()));
        SIMPLE_ITEM_LIST.add(item);
        return item;
    }

    public static Supplier<Item> registerArmorItem(String name, Holder<ArmorMaterial> material, ArmorItem.Type type) {
        Supplier<Item> item = ITEMS.register(name, () -> new MageArmorItem(material, type, getItemProperties()));
        SIMPLE_ITEM_LIST.add(item);
        return item;
    }

    public static Supplier<Item> registerCatalystItem(String name, SpellPath path) {
        Supplier<Item> item = ITEMS.register(name, () -> new CatalystItem(path, getItemProperties()));
        return item;
    }

    public static Supplier<Item> registerItem(String name, Supplier<Item> itemSupplier) {
        Supplier<Item> item = ITEMS.register(name, itemSupplier);
        SIMPLE_ITEM_LIST.add(item);
        return item;
    }

    public static Supplier<Item> registerBlockItem(String name, DeferredBlock<? extends Block> block) {
        Supplier<Item> item = ITEMS.register(name, () -> new BlockItem(block.get(), getItemProperties()));
        BLOCK_ITEM_LIST.add(item);
        return item;
    }

    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
