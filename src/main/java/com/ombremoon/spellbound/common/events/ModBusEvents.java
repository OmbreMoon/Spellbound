package com.ombremoon.spellbound.common.events;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SBAttributes;
import com.ombremoon.spellbound.common.init.SBEntities;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, SBAttributes.MANA_REGEN);
        event.add(EntityType.PLAYER, SBAttributes.MAX_MANA);
    }

    @SubscribeEvent
    public static void onEntityAttributeRegister(EntityAttributeCreationEvent event) {
        SBEntities.SUPPLIERS.forEach(register -> event.put(register.entityTypeSupplier().get(), register.attributeSupplier().get().build()));
    }
}
