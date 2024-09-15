package com.ombremoon.spellbound.networking;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.networking.clientbound.ClientPayloadHandler;
import com.ombremoon.spellbound.networking.clientbound.ClientSyncSkillPayload;
import com.ombremoon.spellbound.networking.clientbound.ClientSyncSpellPayload;
import com.ombremoon.spellbound.networking.serverbound.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadHandler {

    public static void switchMode() {
        PacketDistributor.sendToServer(new SwitchModePayload());
    }

    public static void castSpell(SpellType<?> spellType) {
        PacketDistributor.sendToServer(new CastSpellPayload(spellType));
    }

    public static void cycleSpell() {
        PacketDistributor.sendToServer(new CycleSpellPayload());
    }

    public static void stopChannel() {
        PacketDistributor.sendToServer(new StopChannelPayload());
    }

    public static void syncSpellsToClient(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player,
                new ClientSyncSpellPayload(
                        player.getData(DataInit.SPELL_HANDLER)
                                .serializeNBT(player.level().registryAccess())
                ));
    }

    public static void syncSkillsToClient(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player,
                new ClientSyncSkillPayload(
                        player.getData(DataInit.SKILL_HANDLER)
                                .serializeNBT(player.level().registryAccess())
                ));
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SwitchModePayload.TYPE,
                SwitchModePayload.CODEC,
                ServerPayloadHandler::handleNetworkSwitchMode
        );
        registrar.playToServer(
                CastSpellPayload.TYPE,
                CastSpellPayload.CODEC,
                ServerPayloadHandler::handleNetworkCastSpell
        );
        registrar.playToServer(
                CycleSpellPayload.TYPE,
                CycleSpellPayload.CODEC,
                ServerPayloadHandler::handleNetworkCycleSpell
        );
        registrar.playToServer(
                StopChannelPayload.TYPE,
                StopChannelPayload.CODEC,
                ServerPayloadHandler::handleNetworkStopChannel
        );

        registrar.playToClient(
                ClientSyncSkillPayload.TYPE,
                ClientSyncSkillPayload.CODEC,
                ClientPayloadHandler::handleClientSkillSync
        );
        registrar.playToClient(
                ClientSyncSpellPayload.TYPE,
                ClientSyncSpellPayload.CODEC,
                ClientPayloadHandler::handleClientSpellSync
        );
    }
}
