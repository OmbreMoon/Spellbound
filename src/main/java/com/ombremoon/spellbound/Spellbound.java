package com.ombremoon.spellbound;

import com.ombremoon.spellbound.common.init.SBDataTypes;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

//TODO: General - Discuss Path Shards with team
//TODO: General - Discuss with Duck about bobHurt in GameRenderer

@Mod(Constants.MOD_ID)
public class Spellbound {

    public Spellbound(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerRegistry);
        CommonClass.init(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CommonClass.customLocation("animation"), 42, Spellbound::registerPlayerAnimation);
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }

    private void registerRegistry(NewRegistryEvent event) {
        event.register(SBSpells.REGISTRY);
        event.register(SBSkills.REGISTRY);
        event.register(SBDataTypes.REGISTRY);
    }
}
