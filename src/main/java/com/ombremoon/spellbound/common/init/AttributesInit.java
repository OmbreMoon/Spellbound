package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.w3c.dom.Attr;

public class AttributesInit {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister
            .create(Registries.ATTRIBUTE, Constants.MOD_ID);

    //Mana
    public static Holder<Attribute> MANA_REGEN = register("mana_regen", 1d, 0d, 100d);
    public static Holder<Attribute> MAX_MANA = register("max_mana", 100d, 100d, 5000d);

    //Resistances
    public static Holder<Attribute> MAGIC_RESIST = registerResistance("magic_resistance");
    public static Holder<Attribute> FIRE_SPELL_RESIST = registerResistance("fire_spell_resistance");
    public static Holder<Attribute> FROST_SPELL_RESIST = registerResistance("frost_spell_resistance");
    public static Holder<Attribute> SHOCK_SPELL_RESIST = registerResistance("shock_spell_resistance");
    public static Holder<Attribute> WIND_SPELL_RESIST = registerResistance("wind_spell_resistance");
    public static Holder<Attribute> EARTH_SPELL_RESIST = registerResistance("earth_spell_resistance");
    public static Holder<Attribute> POISON_SPELL_RESIST = registerResistance("poison_spell_resistance");
    public static Holder<Attribute> DISEASE_SPELL_RESIST = registerResistance("disease_spell_resistance");

    public static Holder<Attribute> registerResistance(String name) {
        return register(name, 0d, -100d, 100d);
    }

    public static Holder<Attribute> register(String name, double defaultVal, double min, double max) {
        return ATTRIBUTES.register(name, () -> new RangedAttribute("attribute.name.spellbound." + name,
                defaultVal, min, max));
    }

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
