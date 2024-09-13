package com.ombremoon.spellbound;

import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@Mod(Constants.MOD_ID)
public class Spellbound {

    public Spellbound(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerRegistry);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.Clone.class, event -> {
            if (event.isWasDeath() && event.getOriginal().hasData(DataInit.SPELL_HANDLER.get())) {
                event.getEntity().getData(DataInit.SPELL_HANDLER.get()).deserializeNBT(event.getEntity().registryAccess(), event.getOriginal().getData(DataInit.SPELL_HANDLER.get()).serializeNBT(event.getEntity().registryAccess()));
                event.getEntity().getData(DataInit.SPELL_HANDLER.get()).caster = event.getOriginal().getData(DataInit.SPELL_HANDLER.get()).caster;
            }
        });
        CommonClass.init(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void registerRegistry(NewRegistryEvent event) {
        event.register(SpellInit.REGISTRY);
    }
}
