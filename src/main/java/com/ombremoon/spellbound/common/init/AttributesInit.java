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

    public static Holder<Attribute> MANA_REGEN = ATTRIBUTES.register("mana_regen", () ->
            new RangedAttribute("attribute.name.spellbound.mana_regen",
                    1d, 0d, 5d));

    public static Holder<Attribute> MAX_MANA = ATTRIBUTES.register("max_mana", () ->
            new RangedAttribute("attribute.name.spellbound.max_mana",
                    100d, 0d, 5000d));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
