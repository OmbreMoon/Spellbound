package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Set;
import java.util.function.Supplier;

public class SkillInit {
    public static final ResourceKey<Registry<Skill>> SKILL_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("skill"));
    public static final Registry<Skill> REGISTRY = new RegistryBuilder<>(SKILL_REGISTRY_KEY).sync(true).create();
    public static final DeferredRegister<Skill> SKILLS = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Supplier<Skill> TEST_SKILL = registerSkill("test", 10, Set.of());

    //Wild Mushroom
    public static final Supplier<Skill> VILE_INFLUENCE = registerSkill("vile_influence", 10, Set.of());
    public static final Supplier<Skill> HASTENED_GROWTH = registerSkill("hastened_growth", 10, Set.of(VILE_INFLUENCE));
    public static final Supplier<Skill> ENVENOM = registerSkill("envenom", 10, Set.of(HASTENED_GROWTH));
    public static final Supplier<Skill> DECOMPOSE = registerSkill("decompose", 10, Set.of());
    public static final Supplier<Skill> NATURES_DOMINANCE = registerSkill("natures_dominance", 10, Set.of(DECOMPOSE));
    public static final Supplier<Skill> POISON_ESSENCE = registerSkill("poison_essence", 10, Set.of(NATURES_DOMINANCE));
    public static final Supplier<Skill> CIRCLE_OF_LIFE = registerSkill("circle_of_life", 10, Set.of(POISON_ESSENCE));
    public static final Supplier<Skill> CATALEPSY = registerSkill("catalepsy", 10, Set.of(CIRCLE_OF_LIFE));
    public static final Supplier<Skill> RECYCLED = registerSkill("recycled", 10, Set.of(NATURES_DOMINANCE));
    public static final Supplier<Skill> SYNTHESIS = registerSkill("synthesis", 10, Set.of(POISON_ESSENCE));

    private static Supplier<Skill> registerSkill(String name, int xpCost, Set<Supplier<Skill>> prereqs) {
        return SKILLS.register(name, () -> new Skill(CommonClass.customLocation(name),
               xpCost, prereqs));
    }

    public static void register(IEventBus eventBus) {
        SKILLS.register(eventBus);
    }
}
