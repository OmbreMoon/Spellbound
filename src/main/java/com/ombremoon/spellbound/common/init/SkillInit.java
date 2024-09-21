package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class SkillInit {
    public static final ResourceKey<Registry<Skill>> SKILL_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("skill"));
    public static final Registry<Skill> REGISTRY = new RegistryBuilder<>(SKILL_REGISTRY_KEY).sync(true).create();
    public static final DeferredRegister<Skill> SKILLS = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Holder<Skill> TEST_SKILL = registerSkill("test", SpellInit.TEST_SPELL, 10, HolderSet.empty());

    //Wild Mushroom
    public static final Holder<Skill> WILD_MUSHROOM = registerSkill("wild_mushroom", SpellInit.WILD_MUSHROOM_SPELL);
    public static final Holder<Skill> VILE_INFLUENCE = registerSkill("vile_influence", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(WILD_MUSHROOM));
    public static final Holder<Skill> HASTENED_GROWTH = registerSkill("hastened_growth", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(VILE_INFLUENCE));
    public static final Holder<Skill> ENVENOM = registerSkill("envenom", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(HASTENED_GROWTH));
    public static final Holder<Skill> DECOMPOSE = registerSkill("decompose", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(WILD_MUSHROOM));
    public static final Holder<Skill> NATURES_DOMINANCE = registerSkill("natures_dominance", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(DECOMPOSE));
    public static final Holder<Skill> POISON_ESSENCE = registerSkill("poison_essence", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(NATURES_DOMINANCE));
    public static final Holder<Skill> CIRCLE_OF_LIFE = registerSkill("circle_of_life", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(POISON_ESSENCE));
    public static final Holder<Skill> CATALEPSY = registerSkill("catalepsy", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(CIRCLE_OF_LIFE));
    public static final Holder<Skill> RECYCLED = registerSkill("recycled", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(NATURES_DOMINANCE));
    public static final Holder<Skill> SYNTHESIS = registerSkill("synthesis", SpellInit.WILD_MUSHROOM_SPELL, 10, preReqs(POISON_ESSENCE));

    private static Holder<Skill> registerSkill(String name, Supplier<? extends SpellType<?>> spellType) {
        return SKILLS.register(name, () -> new Skill(spellType, CommonClass.customLocation(name), 0, null));
    }
    private static Holder<Skill> registerSkill(String name, Supplier<? extends SpellType<?>> spellType, int xpCost, HolderSet<Skill> prereqs) {
        return SKILLS.register(name, () -> new Skill(spellType, CommonClass.customLocation(name),
               xpCost, prereqs));
    }

    private static HolderSet<Skill> preReqs(Holder<Skill>... skills) {
        return HolderSet.direct(skills);
    }

    public static void register(IEventBus eventBus) {
        SKILLS.register(eventBus);
    }
}
