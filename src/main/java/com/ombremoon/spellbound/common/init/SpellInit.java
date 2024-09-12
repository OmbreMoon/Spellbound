package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.spell.TestSpell;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class SpellInit {
    public static final ResourceKey<Registry<SpellType<?>>> SPELL_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(CommonClass.customLocation("spell_type"));
    public static final Registry<SpellType<?>> REGISTRY = new RegistryBuilder<>(SPELL_TYPE_REGISTRY_KEY).sync(true).defaultKey(CommonClass.customLocation("test_spell")).create();
    public static final DeferredRegister<SpellType<?>> SPELL_TYPES = DeferredRegister.create(REGISTRY,"spell_type");

    public static final Supplier<SpellType<AnimatedSpell>> TEST_SPELL = registerSpell("test_spell", TestSpell::new);

    private static <T extends AbstractSpell> Supplier<SpellType<T>> registerSpell(String name, SpellType.SpellFactory<T> factory) {
        return SPELL_TYPES.register(name, () -> new SpellType<>(CommonClass.customLocation(name), factory));
    }

    public static void register(IEventBus modEventBus) {
        SPELL_TYPES.register(modEventBus);
    }
}
