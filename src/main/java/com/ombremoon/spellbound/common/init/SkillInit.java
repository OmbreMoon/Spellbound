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

    //Shadow Gate
    public static final Holder<Skill> SHADOW_GATE = registerSkill("shadow_gate");
    public static final Holder<Skill> REACH = registerSkill("reach", -50, 50, preReqs(SHADOW_GATE));
    public static final Holder<Skill> BLINK = registerSkill("blink", -75, 100, preReqs(REACH));
    public static final Holder<Skill> SHADOW_ESCAPE = registerSkill("shadow_escape", -75, 150, preReqs(BLINK));
    public static final Holder<Skill> OPEN_INVITATION = registerSkill("open_invitation", 50, 50, preReqs(SHADOW_GATE));
    public static final Holder<Skill> QUICK_RECHARGE = registerSkill("quick_recharge", 50, 100, preReqs(OPEN_INVITATION));
    public static final Holder<Skill> UNWANTED_GUESTS = registerSkill("unwanted_guests", 100, 100, preReqs(OPEN_INVITATION));
    public static final Holder<Skill> BAIT_AND_SWITCH = registerSkill("bait_and_switch", 100, 150, preReqs(UNWANTED_GUESTS));
    public static final Holder<Skill> DARKNESS_PREVAILS = registerSkill("darkness_prevails", 0, 100, preReqs(SHADOW_GATE));
    public static final Holder<Skill> GRAVITY_SHIFT = registerSkill("gravity_shift", -25, 150, preReqs(DARKNESS_PREVAILS));
    public static final Holder<Skill> DUAL_DESTINATION = registerSkill("dual_destination", 25, 150, preReqs(DARKNESS_PREVAILS));

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

    //Solar Ray
    public static final Holder<Skill> SOLAR_RAY = registerSkill("solar_ray");
    public static final Holder<Skill> SUNSHINE = registerSkill("sunshine", -50, 50, preReqs(SOLAR_RAY));
    public static final Holder<Skill> RADIANCE = registerSkill("radiance", -50, 100, preReqs(SUNSHINE));
    public static final Holder<Skill> HEALING_LIGHT = registerSkill("healing_light", -50, 150, preReqs(RADIANCE));
    public static final Holder<Skill> CONCENTRATED_HEAT = registerSkill("concentrated_heat", 50, 50, preReqs(SOLAR_RAY));
    public static final Holder<Skill> OVERHEAT = registerSkill("overheat", 50, 100, preReqs(CONCENTRATED_HEAT));
    public static final Holder<Skill> SOLAR_BURST = registerSkill("solar_burst", 50, 150, preReqs(OVERHEAT));
    public static final Holder<Skill> REFLECTION = registerSkill("reflection", 100, 200, preReqs(SOLAR_BURST));
    public static final Holder<Skill> BLINDING_LIGHT = registerSkill("blinding_light", 100, 250, preReqs(REFLECTION));
    public static final Holder<Skill> AFTERGLOW = registerSkill("afterglow", 50, 200, preReqs(SOLAR_BURST));
    public static final Holder<Skill> POWER_OF_THE_SUN = registerSkill("power_of_the_sun", 50, 250, preReqs(AFTERGLOW));

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

    //Cobbled hide
    public static final Holder<Skill> COBBLED_HIDE = registerSkill("cobbled_hide");
    public static final Holder<Skill> IRON_HIDE = registerSkill("iron_hide", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> DIAMOND_HIDE = registerSkill("diamond_hide", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> DRAGON_HIDE = registerSkill("dragon_hide", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> THORNY_HIDE = registerSkill("thorny_hide", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> REPULSIVE_SKIN = registerSkill("repulsive_skin", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> REFLECTIVE_FLESH = registerSkill("reflective_flesh", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> BLAST_RESISTANT = registerSkill("blast_resistant", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> SPONGY_FLESH = registerSkill("spongy_flesh", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> INFECTIOUS = registerSkill("infectious", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> REINFORCED = registerSkill("reinforced", 0, 50, preReqs(COBBLED_HIDE));
    public static final Holder<Skill> VIRAL = registerSkill("viral", 0, 50, preReqs(COBBLED_HIDE));

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
