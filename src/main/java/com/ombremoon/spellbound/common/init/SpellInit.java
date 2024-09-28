package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.spell.TestSpell;
import com.ombremoon.spellbound.common.content.spell.divine.HealingTouchSpell;
import com.ombremoon.spellbound.common.content.spell.ruin.VolcanoSpell;
import com.ombremoon.spellbound.common.content.spell.summon.SummonUndeadSpell;
import com.ombremoon.spellbound.common.content.spell.summon.WildMushroomSpell;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class SpellInit {
    public static final ResourceKey<Registry<SpellType<?>>> SPELL_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("spell_type"));
    public static final Registry<SpellType<?>> REGISTRY = new RegistryBuilder<>(SPELL_TYPE_REGISTRY_KEY).sync(true).create();
    public static final DeferredRegister<SpellType<?>> SPELL_TYPES = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Supplier<SpellType<TestSpell>> TEST_SPELL = registerSpell("test_spell", ruinBuilder("test_spell", TestSpell::new));

    //Ruin
    public static final Supplier<SpellType<VolcanoSpell>> VOLCANO = registerSpell("volcano", ruinBuilder("volcano", VolcanoSpell::new)
            .setKeySkill(SkillInit.VOLCANO)
            .setAvailableSkills(
                    SkillInit.INFERNO_CORE, SkillInit.LAVA_FLOW, SkillInit.EXPLOSIVE_BARRAGE,
                    SkillInit.SHRAPNEL, SkillInit.HEATWAVE, SkillInit.SCORCHED_EARTH, SkillInit.SEISMIC_SHOCK,
                    SkillInit.MOLTEN_SHIELD, SkillInit.PYROCLASTIC_CLOUD, SkillInit.APOCALYPSE));

    //Summons
    public static final Supplier<SpellType<SummonUndeadSpell>> SUMMON_UNDEAD_SPELL = registerSpell("summon_undead", summonBuilder("summon_undead", SummonUndeadSpell::new));
    public static final Supplier<SpellType<WildMushroomSpell>> WILD_MUSHROOM_SPELL = registerSpell("wild_mushroom", summonBuilder("wild_mushroom", WildMushroomSpell::new)
            .setKeySkill(SkillInit.WILD_MUSHROOM)
            .setAvailableSkills(
                    SkillInit.VILE_INFLUENCE, SkillInit.HASTENED_GROWTH, SkillInit.ENVENOM,
                    SkillInit.DECOMPOSE, SkillInit.NATURES_DOMINANCE, SkillInit.POISON_ESSENCE,
                    SkillInit.CIRCLE_OF_LIFE, SkillInit.CATALEPSY, SkillInit.RECYCLED, SkillInit.SYNTHESIS));

    //Divine
    public static final Supplier<SpellType<HealingTouchSpell>> HEALING_TOUCH = registerSpell("healing_touch", divineBuilder("healing_touch", HealingTouchSpell::new)
            .setAvailableSkills(
                    SkillInit.HEALING_TOUCH, SkillInit.BLOOM, SkillInit.HEALING_STREAM, SkillInit.ACCELERATED_GROWTH,
                    SkillInit.TRANQUILITY_OF_WATER, SkillInit.NATURES_TOUCH, SkillInit.CLEANSING_TOUCH,
                    SkillInit.OVERGROWTH, SkillInit.VILE_INFLUENCE, SkillInit.CONVALESCENCE));

    private static <T extends AbstractSpell> Supplier<SpellType<T>> registerSpell(String name, SpellType.Builder<T> builder) {
        return SPELL_TYPES.register(name, builder::build);
    }

    private static <T extends AbstractSpell> SpellType.Builder<T> ruinBuilder(String name, SpellType.SpellFactory<T> factory) {
        return new SpellType.Builder<>(name, factory).setPath(SpellPath.RUIN);
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
