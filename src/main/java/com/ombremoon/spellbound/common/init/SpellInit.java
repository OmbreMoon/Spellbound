package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.spell.summon.SummonUndeadSpell;
import com.ombremoon.spellbound.common.content.spell.TestSpell;
import com.ombremoon.spellbound.common.content.spell.summon.WildMushroomSpell;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Set;
import java.util.function.Supplier;

public class SpellInit {
    public static final ResourceKey<Registry<SpellType<?>>> SPELL_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("spell_type"));
    public static final Registry<SpellType<?>> REGISTRY = new RegistryBuilder<>(SPELL_TYPE_REGISTRY_KEY).sync(true).create();
    public static final DeferredRegister<SpellType<?>> SPELL_TYPES = DeferredRegister.create(REGISTRY, Constants.MOD_ID);

    public static final Supplier<SpellType<AnimatedSpell>> TEST_SPELL = registerRuinSpell("test_spell", TestSpell::new);

    //Summons
    public static final Supplier<SpellType<AnimatedSpell>> SUMMON_UNDEAD_SPELL = registerSummonSpell("summon_undead", SummonUndeadSpell::new);
    public static final Supplier<SpellType<AnimatedSpell>> WILD_MUSHROOM_SPELL = registerSummonSpell("wild_mushroom", WildMushroomSpell::new);

    private static <T extends AbstractSpell> Supplier<SpellType<T>> registerRuinSpell(String name, SpellType.SpellFactory<T> factory) {
        return SPELL_TYPES.register(name, () -> new SpellType<>(CommonClass.customLocation(name), SpellPath.RUIN, Set.of(), factory));
    }

    private static <T extends AbstractSpell> Supplier<SpellType<T>> registerSummonSpell(String name, SpellType.SpellFactory<T> factory) {
        return SPELL_TYPES.register(name, () -> new SpellType<>(CommonClass.customLocation(name), SpellPath.SUMMONS, Set.of(), factory));
    }

    public static void register(IEventBus modEventBus) {
        SPELL_TYPES.register(modEventBus);
    }
}
