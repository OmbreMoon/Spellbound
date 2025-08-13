package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.TransfigurationRitual;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.effects.CreateItem;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.effects.CreateSpellTome;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Keys;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public interface SBRituals {
    ResourceKey<TransfigurationRitual> CREATE_STRIDE = key("create_stride");
    ResourceKey<TransfigurationRitual> CREATE_SHADOW_GATE = key("create_shadow_gate");
    ResourceKey<TransfigurationRitual> CREATE_MYSTIC_ARMOR = key("create_mystic_armor");
    ResourceKey<TransfigurationRitual> TEST = key("test");

    static void bootstrap(BootstrapContext<TransfigurationRitual> context) {
        register(
                context,
                CREATE_STRIDE,
                TransfigurationRitual.ritual(1)
                        .requires(Ingredient.of(Items.LEATHER_BOOTS))
                        .requires(Ingredient.of(Items.SUGAR))
                        .requires(Ingredient.of(Items.FEATHER))
                        .requires(Ingredient.of(Items.LAPIS_LAZULI))
                        .withEffect(new CreateSpellTome(SBSpells.STRIDE.get(), 1))

        );
        register(
                context,
                CREATE_SHADOW_GATE,
                TransfigurationRitual.ritual(2)
                        .requires(Ingredient.of(Items.WATER_BUCKET))
                        .requires(Ingredient.of(Items.DIAMOND_SWORD))
                        .requires(Ingredient.of(Items.IRON_INGOT))
                        .requires(Ingredient.of(SBBlocks.ARCANTHUS.get()))
                        .withEffect(new CreateSpellTome(SBSpells.SHADOW_GATE.get(), 2))

        );
        register(
                context,
                CREATE_MYSTIC_ARMOR,
                TransfigurationRitual.ritual(2)
                        .requires(Ingredient.of(Items.DIAMOND_CHESTPLATE))
                        .requires(Ingredient.of(Items.CACTUS))
                        .requires(Ingredient.of(Items.OBSIDIAN), 2)
                        .requires(Ingredient.of(Items.SHIELD), 2)
                        .requires(Ingredient.of(Items.AMETHYST_SHARD), 2)
                        .requires(Ingredient.of(SBItems.MAGIC_ESSENCE.get()), 4)
                        .withEffect(new CreateSpellTome(SBSpells.MYSTIC_ARMOR.get(), 2))

        );

        register(
                context,
                TEST,
                TransfigurationRitual.ritual(2)
                        .requires(Ingredient.of(Items.WATER_BUCKET))
                        .requires(Ingredient.of(Items.GOLD_INGOT))
                        .requires(Ingredient.of(Items.IRON_INGOT))
                        .requires(Ingredient.of(SBBlocks.ARCANTHUS.get()))
                        .withEffect(CreateItem.withData(
                                SBItems.CHALK.get(),
                                new TypedDataComponent<>(SBData.RUNE_INDEX.get(), 3)
                        ))

        );
    }

    private static void register(BootstrapContext<TransfigurationRitual> context, ResourceKey<TransfigurationRitual> key, TransfigurationRitual.Builder builder) {
        context.register(key, builder.build(key.location()));
    }

    private static ResourceKey<TransfigurationRitual> key(String name) {
        return ResourceKey.create(Keys.RITUAL, CommonClass.customLocation(name));
    }
}
