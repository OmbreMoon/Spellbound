package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.network.chat.Component;
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
        handler.setSelectedSpell(payload.spellType());
    }

    public static void handleNetworkCastStart(final CastStartPayload payload, final IPayloadContext context) {
        AbstractSpell spell = payload.spellType().createSpell();
        var spellContext = new SpellContext(context.player(), spell.getTargetEntity(context.player(), 10), payload.recast());
        spell.onCastStart(spellContext);
    }

    public static void handleNetworkCasting(final CastingPayload payload, final IPayloadContext context) {
        AbstractSpell spell = payload.spellType().createSpell();
        var spellContext = new SpellContext(context.player(), spell.getTargetEntity(context.player(), 8), payload.recast());
        spell.whenCasting(spellContext, payload.castTime());
    }

    public static void handleNetworkCastReset(final CastResetPayload payload, final IPayloadContext context) {
        AbstractSpell spell = payload.spellType().createSpell();
        var spellContext = new SpellContext(context.player(), spell.getTargetEntity(context.player(), 10), payload.recast());
        spell.onCastReset(spellContext);
    }

    public static void handleNetworkStopChannel(final StopChannelPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        handler.setChannelling(false);
    }

    public static void handleNetworkUnlockSKill(final UnlockSkillPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSkillHandler(context.player());
        handler.unlockSkill(payload.skill());
        context.player().sendSystemMessage(Component.literal("You have unlocked the " + payload.skill().getName().getString() + " skill"));
    }
}
