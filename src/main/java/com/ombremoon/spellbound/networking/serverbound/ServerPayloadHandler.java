package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.util.SpellUtil;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleNetworkSwitchMode(final SwitchModePayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        handler.switchMode();
    }

    public static void handleNetworkCastSpell(final CastSpellPayload payload, final IPayloadContext context) {
        AbstractSpell spell = payload.spellType().createSpell();
        if (spell != null)
            spell.initSpell(context.player(), context.player().level(), context.player().getOnPos());
    }

    public static void handleNetworkCycleSpell(final CycleSpellPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        SpellUtil.cycle(handler, handler.getSelectedSpell());
    }
}
