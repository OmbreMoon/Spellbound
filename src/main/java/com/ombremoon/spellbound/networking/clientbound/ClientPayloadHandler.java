package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleClientSpellSync(ClientSyncSpellPayload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            SpellHandler handler = new SpellHandler();
            handler.deserializeNBT(context.player().level().registryAccess(), packet.tag());
            handler.caster = context.player();
            context.player().setData(DataInit.SPELL_HANDLER, handler);
        });
    }

    public static void handleClientSkillSync(ClientSyncSkillPayload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            SkillHandler handler = new SkillHandler();
            handler.deserializeNBT(context.player().level().registryAccess(), packet.tag());
            context.player().setData(DataInit.SKILL_HANDLER, handler);
        });
    }
}
