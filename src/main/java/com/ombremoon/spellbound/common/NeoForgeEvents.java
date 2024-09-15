package com.ombremoon.spellbound.common;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.networking.PayloadHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeEvents {

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player player) {
                if (!player.level().isClientSide) {
                    var handler = player.getData(DataInit.SPELL_HANDLER.get());
                    PayloadHandler.syncSpellsToClient(player);
                    handler.initData(player);
                }
            }
        }
    }
}
