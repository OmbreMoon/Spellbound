package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.util.SpellUtil;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleNetworkSwitchMode(final SwitchModePayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        handler.switchMode(payload.castMode());
    }
}
