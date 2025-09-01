package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.common.magic.acquisition.divine.ActionRequirements;
import com.ombremoon.spellbound.common.magic.acquisition.divine.triggers.*;
import com.ombremoon.spellbound.datagen.provider.DivineActionProvider;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.acquisition.divine.ActionHolder;
import com.ombremoon.spellbound.common.magic.acquisition.divine.ActionRewards;
import com.ombremoon.spellbound.common.magic.acquisition.divine.DivineAction;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModDivineActionProvider extends DivineActionProvider {
    protected static final List<EntityType<?>> NON_HOSTILE_MOBS = Arrays.asList(
            EntityType.ALLAY,
            EntityType.ARMADILLO,
            EntityType.AXOLOTL,
            EntityType.BAT,
            EntityType.BEE,
            EntityType.CAMEL,
            EntityType.CAT,
            EntityType.CHICKEN,
            EntityType.COD,
            EntityType.COW,
            EntityType.DOLPHIN,
            EntityType.DONKEY,
            EntityType.FOX,
            EntityType.FROG,
            EntityType.GLOW_SQUID,
            EntityType.GOAT,
            EntityType.HORSE,
            EntityType.IRON_GOLEM,
            EntityType.LLAMA,
            EntityType.MOOSHROOM,
            EntityType.MULE,
            EntityType.OCELOT,
            EntityType.PANDA,
            EntityType.PARROT,
            EntityType.PIG,
            EntityType.POLAR_BEAR,
            EntityType.PUFFERFISH,
            EntityType.RABBIT,
            EntityType.SALMON,
            EntityType.SHEEP,
            EntityType.SNIFFER,
            EntityType.SQUID,
            EntityType.STRIDER,
            EntityType.TADPOLE,
            EntityType.TRADER_LLAMA,
            EntityType.TROPICAL_FISH,
            EntityType.TURTLE,
            EntityType.VILLAGER,
            EntityType.WANDERING_TRADER,
            EntityType.WOLF
    );

    public ModDivineActionProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<ActionHolder> writer) {
        DivineAction.Builder.divineAction()
                .addCriterion("kill_undead",
                    KillActionTrigger.Instance.playerKilledUndead(
                            EntityPredicate.Builder.entity().of(EntityTypeTags.UNDEAD),
                            MinMaxBounds.Ints.exactly(10)))
                .rewards(ActionRewards.Builder.spell(SBSpells.SHADOW_GATE/*SMITE*/.get()).addExperience(20))
                .save(writer, CommonClass.customLocation("kill_undead"));
        createHealToFullAction(writer, NON_HOSTILE_MOBS);
        DivineAction.Builder.divineAction()
                .addCriterion("cured_zombie",
                        CuredZombieVillagerTrigger.TriggerInstance.curedZombieVillager())
                .rewards(ActionRewards.Builder.spell(SBSpells.SOLAR_RAY/*BLESSING*/.get()).addExperience(10))
                .save(writer, CommonClass.customLocation("cured_zombie"));
        DivineAction.Builder.divineAction()
                .addCriterion("kill_villager",
                        KillActionTrigger.Instance.playerKilledVillager(
                                EntityPredicate.Builder.entity().of(EntityTypeTags.UNDEAD),
                                MinMaxBounds.Ints.exactly(5)))
                .rewards(ActionRewards.Builder.spell(SBSpells.STORM_RIFT/*SIPHON*/.get()).addExperience(20))
                .save(writer, CommonClass.customLocation("kill_villager"));
        DivineAction.Builder.divineAction()
                .addCriterion("decorate_shrine",
                        SpecialTrigger.TriggerInstance.decoratedShrine())
                .rewards(ActionRewards.Builder.spell(SBSpells.HEALING_BLOSSOM.get()).addExperience(20))
                .save(writer, CommonClass.customLocation("decorate_shrine"));
    }

    private static void createHealToFullAction(Consumer<ActionHolder> output, List<EntityType<?>> entities) {
        addMobsToHeal(DivineAction.Builder.divineAction(), entities)
                .requirements(ActionRequirements.Strategy.OR)
                .rewards(ActionRewards.Builder.spell(SBSpells.HEALING_TOUCH.get()).addExperience(10))
                .save(output, CommonClass.customLocation("heal_to_full"));
    }

    private static DivineAction.Builder addMobsToHeal(DivineAction.Builder builder, List<EntityType<?>> mobs) {
        mobs.forEach(entityType -> {
            builder.addCriterion(
                    BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString(),
                    HealActionTrigger.Instance.healedToFull(EntityPredicate.Builder.entity().of(entityType)));
        });

        return builder;
    }
}
