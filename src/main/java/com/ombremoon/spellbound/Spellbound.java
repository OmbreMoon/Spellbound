package com.ombremoon.spellbound;

import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
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
//TODO: SummonSpell - Remind Duck of persisting summon bug
//TODO: WorkbenchScreen - Reset position when tab switched

@Mod(Constants.MOD_ID)
public class Spellbound {

    public Spellbound(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerRegistry);
        /*NeoForge.EVENT_BUS.addListener(PlayerEvent.Clone.class, event -> {
            if (event.isWasDeath() && event.getOriginal().hasData(DataInit.SPELL_HANDLER.get())) {
                SpellUtil.getSpellHandler(event.getEntity()).deserializeNBT(event.getEntity().registryAccess(), SpellUtil.getSpellHandler(event.getOriginal()).serializeNBT(event.getEntity().registryAccess()));
                SpellUtil.getSpellHandler(event.getEntity()).caster = SpellUtil.getSpellHandler(event.getOriginal()).caster;
            }
        });*/
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
        event.register(SpellInit.REGISTRY);
        event.register(SkillInit.REGISTRY);
    }
}
