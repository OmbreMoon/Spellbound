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

    public static final Holder<Skill> TEST_SKILL = registerSkill("test");

    //Wild Mushroom
    public static final Holder<Skill> WILD_MUSHROOM = registerSkill("wild_mushroom");
//    public static final Holder<Skill> VILE_INFLUENCE = registerSkill("vile_influence", 10, 0, 0, preReqs(WILD_MUSHROOM));
//    public static final Holder<Skill> HASTENED_GROWTH = registerSkill("hastened_growth", 10, 0, 0, preReqs(VILE_INFLUENCE));
//    public static final Holder<Skill> ENVENOM = registerSkill("envenom", 10, 0, 0, preReqs(HASTENED_GROWTH));
//    public static final Holder<Skill> DECOMPOSE = registerSkill("decompose", 10, 0, 0, preReqs(WILD_MUSHROOM));
//    public static final Holder<Skill> NATURES_DOMINANCE = registerSkill("natures_dominance", 10, 0, 0, preReqs(DECOMPOSE));
//    public static final Holder<Skill> POISON_ESSENCE = registerSkill("poison_essence", 10, 0, 0, preReqs(NATURES_DOMINANCE));
//    public static final Holder<Skill> CIRCLE_OF_LIFE = registerSkill("circle_of_life", 10, 0, 0, preReqs(POISON_ESSENCE));
//    public static final Holder<Skill> CATALEPSY = registerSkill("catalepsy", 10, 0, 0, preReqs(CIRCLE_OF_LIFE));
//    public static final Holder<Skill> RECYCLED = registerSkill("recycled", 10, 0, 0, preReqs(NATURES_DOMINANCE));
//    public static final Holder<Skill> SYNTHESIS = registerSkill("synthesis", 10, 0, 0, preReqs(POISON_ESSENCE));

    //Volcano
    public static final Holder<Skill> VOLCANO = registerSkill("volcano");
    public static final Holder<Skill> INFERNO_CORE = registerSkill("inferno_core", 10, 0, 50, preReqs(VOLCANO));
    public static final Holder<Skill> LAVA_FLOW = registerSkill("lava_flow", 10, 50, 50, preReqs(VOLCANO));
    public static final Holder<Skill> EXPLOSIVE_BARRAGE = registerSkill("explosive_barrage", 10, -50, 50, preReqs(VOLCANO));
    public static final Holder<Skill> SHRAPNEL = registerSkill("shrapnel", 10, -50, 100, preReqs(EXPLOSIVE_BARRAGE));
    public static final Holder<Skill> HEATWAVE = registerSkill("heatwave", 10, -100, 100, preReqs(EXPLOSIVE_BARRAGE));
    public static final Holder<Skill> SCORCHED_EARTH = registerSkill("scorched_earth", 10, -100, 150, preReqs(HEATWAVE));
    public static final Holder<Skill> SEISMIC_SHOCK = registerSkill("seismic_shock", 10, 50, 100, preReqs(LAVA_FLOW));
    public static final Holder<Skill> MOLTEN_SHIELD = registerSkill("molten_shield", 10, 100, 100, preReqs(LAVA_FLOW));
    public static final Holder<Skill> PYROCLASTIC_CLOUD = registerSkill("pyroclastic_cloud", 10, 0, 100, preReqs(INFERNO_CORE));
    public static final Holder<Skill> APOCALYPSE = registerSkill("apocalypse", 10, 0, 175, preReqs(SHRAPNEL, PYROCLASTIC_CLOUD, SEISMIC_SHOCK));

    private static Holder<Skill> registerSkill(String name) {
        return SKILLS.register(name, () -> new Skill(CommonClass.customLocation(name)));
    }
    private static Holder<Skill> registerSkill(String name, int xpCost, int xPos, int yPos, HolderSet<Skill> prereqs) {
        return SKILLS.register(name, () -> new Skill(CommonClass.customLocation(name),
               xpCost, xPos, yPos, prereqs));
    }

    private static HolderSet<Skill> preReqs(Holder<Skill>... skills) {
        return HolderSet.direct(skills);
    }

    public static void register(IEventBus eventBus) {
        SKILLS.register(eventBus);
    }
}
