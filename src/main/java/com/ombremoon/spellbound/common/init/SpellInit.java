package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.spell.TestSpell;
import com.ombremoon.spellbound.common.content.spell.deception.ShadowbondSpell;
import com.ombremoon.spellbound.common.content.spell.divine.HealingTouchSpell;
import com.ombremoon.spellbound.common.content.spell.ruin.fire.SolarRaySpell;
import com.ombremoon.spellbound.common.content.spell.ruin.fire.VolcanoSpell;
import com.ombremoon.spellbound.common.content.spell.ruin.shock.ElectricChargeSpell;
import com.ombremoon.spellbound.common.content.spell.summon.SpiritTotemSpell;
import com.ombremoon.spellbound.common.content.spell.summon.SummonUndeadSpell;
import com.ombremoon.spellbound.common.content.spell.summon.WildMushroomSpell;
import com.ombremoon.spellbound.common.content.spell.transfiguration.ShadowGateSpell;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class SpellInit {
    public static final ResourceKey<Registry<SpellType<?>>> SPELL_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("spell_type"));
    public static final Registry<SpellType<?>> REGISTRY = new RegistryBuilder<>(SPELL_TYPE_REGISTRY_KEY).sync(true).create();
    public static final DeferredRegister<SpellType<?>> SPELL_TYPES = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Supplier<SpellType<TestSpell>> TEST_SPELL = registerSpell("test_spell", fireRuinBuilder("test_spell", TestSpell::new));

    //Ruin
    public static final Supplier<SpellType<SolarRaySpell>> SOLAR_RAY = registerSpell("solar_ray", fireRuinBuilder("solar_ray", SolarRaySpell::new)
            .skills(SkillInit.SOLAR_RAY, SkillInit.SUNSHINE, SkillInit.RADIANCE, SkillInit.HEALING_LIGHT,
                    SkillInit.CONCENTRATED_HEAT, SkillInit.OVERHEAT, SkillInit.SOLAR_BURST,
                    SkillInit.SOLAR_BORE, SkillInit.BLINDING_LIGHT, SkillInit.AFTERGLOW, SkillInit.POWER_OF_THE_SUN));
    public static final Supplier<SpellType<VolcanoSpell>> VOLCANO = registerSpell("volcano", fireRuinBuilder("volcano", VolcanoSpell::new)
            .skills(SkillInit.VOLCANO, SkillInit.INFERNO_CORE, SkillInit.LAVA_FLOW, SkillInit.EXPLOSIVE_BARRAGE,
                    SkillInit.SHRAPNEL, SkillInit.HEATWAVE, SkillInit.SCORCHED_EARTH, SkillInit.SEISMIC_SHOCK,
                    SkillInit.MOLTEN_SHIELD, SkillInit.PYROCLASTIC_CLOUD, SkillInit.APOCALYPSE));
    public static final Supplier<SpellType<ElectricChargeSpell>> ELECTRIC_CHARGE = registerSpell("electric_charge", shockRuinBuilder("electric_charge", ElectricChargeSpell::new)
            .skills(SkillInit.ELECTRIC_CHARGE, SkillInit.ELECTRIFICATION, SkillInit.SUPERCONDUCTOR, SkillInit.CYCLONIC_FURY,
                    SkillInit.OSCILLATION, SkillInit.HIGH_VOLTAGE, SkillInit.UNLEASHED_STORM, SkillInit.STORM_SURGE,
                    SkillInit.CHAIN_REACTION, SkillInit.AMPLIFY, SkillInit.ALTERNATING_CURRENT));

    //Transfiguration
    public static final Supplier<SpellType<ShadowGateSpell>> SHADOW_GATE = registerSpell("shadow_gate", trasnfigurationBuilder("shadow_gate", ShadowGateSpell::new)
            .skills(SkillInit.SHADOW_GATE, SkillInit.REACH, SkillInit.BLINK, SkillInit.SHADOW_ESCAPE,
                    SkillInit.OPEN_INVITATION, SkillInit.QUICK_RECHARGE, SkillInit.UNWANTED_GUESTS, SkillInit.BAIT_AND_SWITCH,
                    SkillInit.DARKNESS_PREVAILS, SkillInit.GRAVITY_SHIFT, SkillInit.DUAL_DESTINATION));

    //Summons
    public static final Supplier<SpellType<SummonUndeadSpell>> SUMMON_UNDEAD = registerSpell("summon_undead", summonBuilder("summon_undead", SummonUndeadSpell::new));
    public static final Supplier<SpellType<WildMushroomSpell>> WILD_MUSHROOM = registerSpell("wild_mushroom", summonBuilder("wild_mushroom", WildMushroomSpell::new)
            .skills(SkillInit.WILD_MUSHROOM, SkillInit.VILE_INFLUENCE, SkillInit.HASTENED_GROWTH, SkillInit.ENVENOM,
                    SkillInit.DECOMPOSE, SkillInit.NATURES_DOMINANCE, SkillInit.POISON_ESSENCE,
                    SkillInit.CIRCLE_OF_LIFE, SkillInit.CATALEPSY, SkillInit.RECYCLED, SkillInit.SYNTHESIS));
    public static final Supplier<SpellType<SpiritTotemSpell>> SPIRIT_TOTEM = registerSpell("conjure_spirit_totem", summonBuilder("conjure_spirit_totem", SpiritTotemSpell::new)
            .skills(SkillInit.CONJURE_SPIRIT_TOTEM, SkillInit.CATS_AGILITY, SkillInit.FERAL_FURY,
                    SkillInit.PRIMAL_RESILIENCE, SkillInit.TOTEMIC_BOND, SkillInit.STEALTH_TACTIC, SkillInit.SAVAGE_LEAP,
                    SkillInit.TOTEMIC_ARMOR, SkillInit.WARRIORS_ROAR, SkillInit.TWIN_SPIRITS, SkillInit.NINE_LIVES));

    //Divine
    public static final Supplier<SpellType<HealingTouchSpell>> HEALING_TOUCH = registerSpell("healing_touch", divineBuilder("healing_touch", HealingTouchSpell::new)
            .skills(SkillInit.HEALING_TOUCH, SkillInit.DIVINE_BALANCE, SkillInit.HEALING_STREAM, SkillInit.ACCELERATED_GROWTH,
                    SkillInit.TRANQUILITY_OF_WATER, SkillInit.NATURES_TOUCH, SkillInit.CLEANSING_TOUCH,
                    SkillInit.OVERGROWTH, SkillInit.BLASPHEMY, SkillInit.CONVALESCENCE));

    //Deception
    public static final Supplier<SpellType<ShadowbondSpell>> SHADOWBOND = registerSpell("shadowbond", deceptionBuilder("shadowbond", ShadowbondSpell::new)
            .skills(SkillInit.SHADOWBOND, SkillInit.EVERLASTING_BOND, SkillInit.SHADOW_STEP, SkillInit.SNEAK_ATTACK,
                    SkillInit.SILENT_EXCHANGE, SkillInit.SNARE, SkillInit.DISORIENTED,
                    SkillInit.OBSERVANT, SkillInit.LIVING_SHADOW, SkillInit.REVERSAL, SkillInit.SHADOW_CHAIN));

    private static <T extends AbstractSpell> Supplier<SpellType<T>> registerSpell(String name, SpellType.Builder<T> builder) {
        return SPELL_TYPES.register(name, builder::build);
    }

    private static <T extends AbstractSpell> SpellType.Builder<T> fireRuinBuilder(String name, SpellType.SpellFactory<T> factory) {
        return new SpellType.Builder<>(name, factory).setPath(SpellPath.RUIN, SpellPath.FIRE);
    }

    private static <T extends AbstractSpell> SpellType.Builder<T> shockRuinBuilder(String name, SpellType.SpellFactory<T> factory) {
        return new SpellType.Builder<>(name, factory).setPath(SpellPath.RUIN, SpellPath.SHOCK);
    }

    private static <T extends AbstractSpell> SpellType.Builder<T> trasnfigurationBuilder(String name, SpellType.SpellFactory<T> factory) {
        return new SpellType.Builder<>(name, factory).setPath(SpellPath.TRANSFIGURATION);
    }

    private static <T extends AbstractSpell> SpellType.Builder<T> summonBuilder(String name, SpellType.SpellFactory<T> factory) {
        return new SpellType.Builder<>(name, factory).setPath(SpellPath.SUMMONS);
    }

    private static <T extends AbstractSpell> SpellType.Builder<T> divineBuilder(String name, SpellType.SpellFactory<T> factory) {
        return new SpellType.Builder<>(name, factory).setPath(SpellPath.DIVINE);
    }

    private static <T extends AbstractSpell> SpellType.Builder<T> deceptionBuilder(String name, SpellType.SpellFactory<T> factory) {
        return new SpellType.Builder<>(name, factory).setPath(SpellPath.DECEPTION);
    }

    public static void register(IEventBus modEventBus) {
        SPELL_TYPES.register(modEventBus);
    }
}
