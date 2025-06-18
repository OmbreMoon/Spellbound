package com.ombremoon.spellbound.main;

import com.ombremoon.spellbound.client.shader.SBShaders;
import com.ombremoon.spellbound.common.content.world.multiblock.Multiblock;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.TransfigurationRitual;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NewRegistryEvent;

//TODO: General - Discuss with Duck about bobHurt in GameRenderer
//TODO: General - Discuss with Duck about Followers

@Mod(Constants.MOD_ID)
public class Spellbound {

    public Spellbound(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) -> {
            event.dataPackRegistry(Keys.RITUAL, TransfigurationRitual.DIRECT_CODEC, TransfigurationRitual.DIRECT_CODEC);
            event.dataPackRegistry(Keys.MULTIBLOCKS, Multiblock.CODEC);
        });
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerRegistry);
        CommonClass.init(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CommonClass.customLocation("animation"), 42, Spellbound::registerPlayerAnimation);

        for (SpellPath spellPath : SpellPath.values()) {
            if (!spellPath.isSubPath()) {
                ItemProperties.register(SBItems.SPELL_TOME.get(), CommonClass.customLocation(spellPath.getSerializedName()), (stack, level, entity, seed) -> {
                    String path = stack.get(SBData.SPELL);
                    if (path != null) {
                        ResourceLocation location = ResourceLocation.tryParse(path);
                        if (location != null) {
                            SpellType<?> spellType = SBSpells.REGISTRY.get(location);
                            if (spellType != null) {
                                SpellPath spellPath1 = spellType.getPath();
                                return spellPath == spellPath1 ? 1.0F : 0.0F;
                            }
                        }
                    }

                    return 0.0F;
                });
            }
        }
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }

    private void registerRegistry(NewRegistryEvent event) {
        event.register(SBSpells.REGISTRY);
        event.register(SBSkills.REGISTRY);
        event.register(SBDataTypes.REGISTRY);
        event.register(SBTriggers.REGISTRY);
        event.register(SBMultiblockSerializers.REGISTRY);
        event.register(SBRitualEffects.REGISTRY);
    }
}
