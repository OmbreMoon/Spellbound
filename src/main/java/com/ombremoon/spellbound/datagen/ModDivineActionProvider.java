package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.common.magic.acquisition.divine.triggers.*;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.acquisition.divine.ActionHolder;
import com.ombremoon.spellbound.common.magic.acquisition.divine.ActionRewards;
import com.ombremoon.spellbound.common.magic.acquisition.divine.DivineAction;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EntityTypeTags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModDivineActionProvider extends DivineActionProvider {
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
        DivineAction.Builder.divineAction()
                .addCriterion("heal_to_full",
                        HealActionTrigger.Instance.healed(
                                HealPredicate.healed(MinMaxBounds.Doubles.atLeast(19))))
                .rewards(ActionRewards.Builder.spell(SBSpells.HEALING_TOUCH.get()).addExperience(10))
                .save(writer, CommonClass.customLocation("heal_to_full"));
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
    }
}
