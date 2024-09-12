package com.ombremoon.spellbound;

import com.ombremoon.spellbound.client.event.SpellCastEvents;
import com.ombremoon.spellbound.common.capability.ISpellHandler;
import com.ombremoon.spellbound.common.capability.SpellHandler;
import com.ombremoon.spellbound.common.init.SpellInit;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.CapabilityRegistry;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.jetbrains.annotations.Nullable;

@Mod(Constants.MOD_ID)
public class Spellbound {
    //public static final EntityCapability<ISpellHandler, @Nullable Void> ENTITY = EntityCapability.createVoid(CommonClass.customLocation("spell_handler"), ISpellHandler.class);

    public Spellbound(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerRegistry);
        NeoForge.EVENT_BUS.addListener(SpellCastEvents::onSpellCast);
        CommonClass.init(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        //event.registerEntity(ENTITY, EntityType.PLAYER, ((player, context) -> new SpellHandler()));
        Constants.LOG.info("fuck this");
    }

    private void registerRegistry(NewRegistryEvent event) {
        event.register(SpellInit.REGISTRY);
    }
}
