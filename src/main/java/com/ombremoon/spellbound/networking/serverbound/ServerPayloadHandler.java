package com.ombremoon.spellbound.networking.serverbound;

import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleNetworkSwitchMode(final SwitchModePayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellCaster(context.player());
        handler.switchMode();
    }

    public static void handleNetworkCastSpell(final CastSpellPayload payload, final IPayloadContext context) {
        var level = context.player().level();
        if (!level.isClientSide) {
            var handler = SpellUtil.getSpellCaster(context.player());
            AbstractSpell spell = handler.getCurrentlyCastSpell();
            spell.initSpell(context.player());
//            handler.setCurrentlyCastingSpell(null);
        }
    }

    public static void handleNetworkSetSpell(final SetSpellPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellCaster(context.player());
        handler.setSelectedSpell(payload.spellType());
    }

    public static void handleNetworkEquipSpell(final EquipSpellPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellCaster(context.player());
        if (payload.equip()) {
            handler.equipSpell(payload.spellType());
        } else {
            handler.unequipSpell(payload.spellType());
        }
    }

    public static void handleNetworkSetCastSpell(final SetCastingSpellPayload payload, final IPayloadContext context) {
        Level level = context.player().level();
        var spellContext = new SpellContext(payload.spellType(), context.player(), level.getEntity(payload.targetID()), payload.isRecast());
        AbstractSpell spell = payload.spellType().createSpell();
        var handler = SpellUtil.getSpellCaster(context.player());
        spell.setCastContext(spellContext);
        handler.setCurrentlyCastingSpell(spell);
    }

    public static void handleNetworkCastStart(final CastStartPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellCaster(context.player());
        AbstractSpell spell = handler.getCurrentlyCastSpell();
        if (spell != null)
            spell.onCastStart(spell.getCastContext());
    }

    public static void handleNetworkCastReset(final CastResetPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellCaster(context.player());
        AbstractSpell spell = handler.getCurrentlyCastSpell();
        spell.onCastReset(spell.getCastContext());
    }

    public static void handleNetworkUpdateFlag(final UpdateFlagPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellCaster(context.player());
        handler.setFlag(payload.spellType(), payload.flag());
    }

    public static void handleNetworkChargeOrChannel(final ChargeOrChannelPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellCaster(context.player());
        handler.setChargingOrChannelling(payload.isChargingOrChannelling());
    }

    public static void handleNetworkUnlockSkill(final UnlockSkillPayload payload, final IPayloadContext context) {
        var holder = SpellUtil.getSkills(context.player());
        holder.unlockSkill(payload.skill(), true);
        context.player().sendSystemMessage(Component.literal("You have unlocked the " + payload.skill().getName().getString() + " skill"));
    }

    public static void handleNetworkPlayerMovement(final PlayerMovementPayload payload, final IPayloadContext context) {
        Player player = context.player();
        var caster = SpellUtil.getSpellCaster(player);
        if (payload.movement() == PlayerMovementPayload.Movement.MOVE) {
            caster.forwardImpulse = payload.forwardImpulse();
            caster.leftImpulse = payload.leftImpulse();
        } else if (payload.movement() == PlayerMovementPayload.Movement.ROTATE) {
            player.setYBodyRot(payload.yRot());
        }
    }
}
