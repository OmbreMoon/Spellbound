package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.networking.serverbound.SwitchModePayload;
import com.ombremoon.spellbound.util.SpellUtil;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleMainSwitchMode(final SwitchModePayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        handler.switchMode(payload.castMode());
    }
}
