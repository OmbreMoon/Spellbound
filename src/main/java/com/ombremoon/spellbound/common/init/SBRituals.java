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
    ResourceKey<TransfigurationRitual> CREATE_SHADOW_GATE = key("create_shadow_gate");
    ResourceKey<TransfigurationRitual> TEST = key("test");

    static void bootstrap(BootstrapContext<TransfigurationRitual> context) {
        register(
                context,
                CREATE_SHADOW_GATE,
                TransfigurationRitual.ritual(1)
                        .requires(Ingredient.of(Items.WATER_BUCKET))
                        .requires(Ingredient.of(Items.DIAMOND_SWORD))
                        .requires(Ingredient.of(Items.IRON_INGOT))
                        .requires(Ingredient.of(SBBlocks.ARCANTHUS.get()))
                        .withEffect(new CreateSpellTome(SBSpells.SHADOW_GATE.get(), 1))

        );

        register(
                context,
                TEST,
                TransfigurationRitual.ritual(1)
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
