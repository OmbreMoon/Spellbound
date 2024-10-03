package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.magic.SpellModifier;
import com.ombremoon.spellbound.common.magic.skills.ModifierSkill;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

@SuppressWarnings("unchecked")
public class SkillInit {
    public static final ResourceKey<Registry<Skill>> SKILL_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("skill"));
    public static final Registry<Skill> REGISTRY = new RegistryBuilder<>(SKILL_REGISTRY_KEY).sync(true).create();
    public static final DeferredRegister<Skill> SKILLS = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Holder<Skill> TEST_SKILL = registerSkill("test");

    //Wild Mushroom
    public static final Holder<Skill> WILD_MUSHROOM = registerSkill("wild_mushroom");
    public static final Holder<Skill> VILE_INFLUENCE = registerSkill("vile_influence", -50, 50, preReqs(WILD_MUSHROOM));
    public static final Holder<Skill> HASTENED_GROWTH = registerSkill("hastened_growth", -50, 100, preReqs(VILE_INFLUENCE));
    public static final Holder<Skill> ENVENOM = registerSkill("envenom", -50, 150, preReqs(HASTENED_GROWTH));
    public static final Holder<Skill> DECOMPOSE = registerSkill("decompose", 50, 50, preReqs(WILD_MUSHROOM));
    public static final Holder<Skill> NATURES_DOMINANCE = registerSkill("natures_dominance", 50, 100, preReqs(DECOMPOSE));
    public static final Holder<Skill> POISON_ESSENCE = registerSkill("poison_essence", 50, 150, preReqs(NATURES_DOMINANCE));
    public static final Holder<Skill> CIRCLE_OF_LIFE = registerSkill("circle_of_life", 50, 200, preReqs(POISON_ESSENCE));
    public static final Holder<Skill> CATALEPSY = registerSkill("catalepsy", 50, 250, preReqs(CIRCLE_OF_LIFE));
    public static final Holder<Skill> RECYCLED = registerSkill("recycled", 100, 150, preReqs(NATURES_DOMINANCE));
    public static final Holder<Skill> SYNTHESIS = registerSkill("synthesis", 100, 200, preReqs(POISON_ESSENCE));

    //Volcano
    public static final Holder<Skill> VOLCANO = registerSkill("volcano");
    public static final Holder<Skill> INFERNO_CORE = registerSkill("inferno_core", 0, 50, preReqs(VOLCANO));
    public static final Holder<Skill> LAVA_FLOW = registerSkill("lava_flow", 50, 50, preReqs(VOLCANO));
    public static final Holder<Skill> EXPLOSIVE_BARRAGE = registerSkill("explosive_barrage", -50, 50, preReqs(VOLCANO));
    public static final Holder<Skill> SHRAPNEL = registerSkill("shrapnel", -50, 100, preReqs(EXPLOSIVE_BARRAGE));
    public static final Holder<Skill> HEATWAVE = registerSkill("heatwave", -100, 100, preReqs(EXPLOSIVE_BARRAGE));
    public static final Holder<Skill> SCORCHED_EARTH = registerSkill("scorched_earth", 50, 100, preReqs(LAVA_FLOW));
    public static final Holder<Skill> SEISMIC_SHOCK = registerSkill("seismic_shock", -100, 150, preReqs(HEATWAVE));
    public static final Holder<Skill> MOLTEN_SHIELD = registerSkill("molten_shield", 100, 100, preReqs(LAVA_FLOW));
    public static final Holder<Skill> PYROCLASTIC_CLOUD = registerSkill("pyroclastic_cloud", 0, 100, preReqs(INFERNO_CORE));
    public static final Holder<Skill> APOCALYPSE = registerSkill("apocalypse", 0, 175, preReqs(SHRAPNEL, PYROCLASTIC_CLOUD, SCORCHED_EARTH));

    //Healing Touch
    //TODO: Tree
    public static final Holder<Skill> HEALING_TOUCH = registerSkill("healing_touch");
    public static final Holder<Skill> DIVINE_BALANCE = registerModifierSkill("divine_balance", 0, 50, preReqs(HEALING_TOUCH), SpellModifier.DIVINE_BALANCE_DURATION, SpellModifier.DIVINE_BALANCE_MANA);
    public static final Holder<Skill> HEALING_STREAM = registerSkill("healing_stream", 0, 50, preReqs(HEALING_TOUCH));
    public static final Holder<Skill> ACCELERATED_GROWTH = registerSkill("accelerated_growth", 0, 50, preReqs(HEALING_TOUCH));
    public static final Holder<Skill> TRANQUILITY_OF_WATER = registerSkill("tranquility_of_water", 0, 50, preReqs(HEALING_TOUCH));
    public static final Holder<Skill> NATURES_TOUCH = registerSkill("natures_touch", 0, 50, preReqs(HEALING_TOUCH));
    public static final Holder<Skill> CLEANSING_TOUCH = registerSkill("cleansing_touch", 0, 50, preReqs(HEALING_TOUCH));
    public static final Holder<Skill> OVERGROWTH = registerSkill("overgrowth", 0, 50, preReqs(HEALING_TOUCH));
    public static final Holder<Skill> BLASPHEMY = registerSkill("blasphemy", -50, 50, preReqs(HEALING_TOUCH)); //TODO
    public static final Holder<Skill> CONVALESCENCE = registerSkill("convalescence", -50, 100, preReqs(HEALING_TOUCH));
    public static final Holder<Skill> OAK_BLESSING = registerSkill("oak_blessing", 0, 50, preReqs(HEALING_STREAM));

    private static Holder<Skill> registerSkill(String name) {
        return SKILLS.register(name, () -> new Skill(CommonClass.customLocation(name)));
    }
    private static Holder<Skill> registerSkill(String name, int xPos, int yPos, HolderSet<Skill> prereqs) {
        return SKILLS.register(name, () -> new Skill(CommonClass.customLocation(name), xPos, yPos, prereqs));
    }
    private static Holder<Skill> registerModifierSkill(String name, int xPos, int yPos, HolderSet<Skill> prereqs, SpellModifier... spellModifiers) {
        return SKILLS.register(name, () -> new ModifierSkill(CommonClass.customLocation(name), xPos, yPos, prereqs, spellModifiers));
    }

    private static HolderSet<Skill> preReqs(Holder<Skill>... skills) {
        return HolderSet.direct(skills);
    }

    public static void register(IEventBus eventBus) {
        SKILLS.register(eventBus);
    }
}
