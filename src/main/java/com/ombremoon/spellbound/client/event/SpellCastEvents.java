package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class SpellCastEvents {

    @SubscribeEvent
    public static void onSpellCast(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled())
            return;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            var handler = SpellUtil.getSpellHandler(player);
            Constants.LOG.info("{}", handler.inCastMode());
        }
    }

    private boolean isAbleToSpellCast() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getOverlay() != null)
            return false;
        if (minecraft.screen != null)
            return false;
        if (!minecraft.mouseHandler.isMouseGrabbed())
            return false;
        return minecraft.isWindowActive();
    }

    public void castSpell(Player player, SpellType<?> spellType) {
        if (player.isSpectator()) {
            return;
        }

        if (spellType != null) {

        }
    }
}
