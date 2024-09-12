package com.ombremoon.spellbound.networking;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.networking.clientbound.ClientPayloadHandler;
import com.ombremoon.spellbound.networking.clientbound.ClientSyncSpellPacket;
import com.ombremoon.spellbound.networking.serverbound.ServerPayloadHandler;
import com.ombremoon.spellbound.networking.serverbound.SwitchModePayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadHandler {

    public static void switchMode(boolean castMode) {
        PacketDistributor.sendToServer(new SwitchModePayload(castMode));
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SwitchModePayload.TYPE,
                SwitchModePayload.CODEC,
                new DirectionalPayloadHandler<>(
                        ClientPayloadHandler::handleMainSwitchMode,
                        ServerPayloadHandler::handleNetworkSwitchMode
                )
        );

        registrar.playToClient(
                ClientSyncSpellPacket.TYPE,
                ClientSyncSpellPacket.STREAM_CODEC,
                ClientSyncSpellPacket::handleData
        );
    }
}
