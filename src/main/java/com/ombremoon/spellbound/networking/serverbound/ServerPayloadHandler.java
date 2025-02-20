package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
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
        var level = context.player().level();
        if (!level.isClientSide) {
            var handler = SpellUtil.getSpellHandler(context.player());
            AbstractSpell spell = handler.getCurrentlyCastSpell();
            spell.initSpell(context.player(), level, context.player().getOnPos());
            handler.setCurrentlyCastingSpell(null);
        }
    }

    public static void handleNetworkSetSpell(final SetSpellPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        handler.setSelectedSpell(payload.spellType());
    }

    public static void handleNetworkSetCastSpell(final SetCastingSpellPayload payload, final IPayloadContext context) {
        var spellContext = new SpellContext(payload.spellType(), context.player(), payload.isRecast());
        AbstractSpell spell = payload.spellType().createSpell();
        var handler = SpellUtil.getSpellHandler(context.player());
        spell.setCastContext(spellContext);
        handler.setCurrentlyCastingSpell(spell);

    }

    public static void handleNetworkCastStart(final CastStartPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        AbstractSpell spell = handler.getCurrentlyCastSpell();
        spell.onCastStart(spell.getCastContext());
    }

    public static void handleNetworkCasting(final CastingPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        AbstractSpell spell = handler.getCurrentlyCastSpell();
        spell.whenCasting(spell.getCastContext(), payload.castTime());
    }

    public static void handleNetworkCastReset(final CastResetPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        AbstractSpell spell = handler.getCurrentlyCastSpell();
        spell.onCastReset(spell.getCastContext());
    }

    public static void handleNetworkUpdateFlag(final UpdateFlagPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        handler.setFlag(payload.spellType(), payload.flag());
    }

    public static void handleNetworkSetCastKey(final SetCastKeyPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        handler.castKeyDown = payload.isDown();
    }

    public static void handleNetworkStopChannel(final StopChannelPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellHandler(context.player());
        handler.setChannelling(false);
    }

    public static void handleNetworkUnlockSKill(final UnlockSkillPayload payload, final IPayloadContext context) {
        var holder = SpellUtil.getSkillHolder(context.player());
        holder.unlockSkill(payload.skill());
        context.player().sendSystemMessage(Component.literal("You have unlocked the " + payload.skill().getName().getString() + " skill"));
    }
}
