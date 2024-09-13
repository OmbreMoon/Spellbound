package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.client.KeyBinds;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class SpellCastEvents {

    @SubscribeEvent
    public static void onSpellMode(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled()) return;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            var handler = SpellUtil.getSpellHandler(player);
            var spellType = handler.getSelectedSpell();

            if (spellType == null) return;

            if (handler.inCastMode()) {
                if (KeyBinds.getSpellCastMapping().isDown()) {
                    if (player.isCrouching()) {
                        SpellUtil.cycle(handler, spellType);
                        PayloadHandler.cycleSpell();
                        KeyBinds.getSpellCastMapping().setDown(false);
                    }
                    event.setSwingHand(false);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onChargeOrChannelSpell(ClientTickEvent.Post event) {
        if (!isAbleToSpellCast())
            return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        var handler = SpellUtil.getSpellHandler(player);
        if (!handler.inCastMode()) return;

        var spellType = handler.getSelectedSpell();
        if (spellType == null) return;

        if (player.tickCount % 40 == 0) Constants.LOG.info("Mode");
        if (!handler.isChannelling()) {
            if (KeyBinds.getSpellCastMapping().isDown()) {
                AbstractSpell spell = spellType.createSpell();
                int castTime = spell.getCastTime();
                if (handler.castTick >= castTime) {
                    KeyBinds.getSpellCastMapping().setDown(false);
                    castSpell(player, spellType);
                    handler.castTick = 0;
                } else {
                    handler.castTick++;
                }
            }
        } else {
            if (!KeyBinds.getSpellCastMapping().isDown())
                handler.setChannelling(false);
        }
    }

    @SubscribeEvent
    public static void onSpellActivateTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide) {
            var handler = SpellUtil.getSpellHandler(player);
            ObjectOpenHashSet<AbstractSpell> activeSpells = handler.getActiveSpells();

            activeSpells.removeIf(spell -> spell.isInactive);
            for (AbstractSpell abstractSpell : activeSpells) {
                abstractSpell.tick();
            }
        }
    }

    private static boolean isAbleToSpellCast() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getOverlay() != null) return false;
        if (minecraft.screen != null) return false;
        if (!minecraft.mouseHandler.isMouseGrabbed()) return false;
        return minecraft.isWindowActive();
    }

    public static void castSpell(Player player, SpellType<?> spellType) {
        AbstractSpell spell = spellType.createSpell();
        if (player.isSpectator()) return;
        if (spell == null) return;
        if (!SpellUtil.canCastSpell(player, spell)) return;

        PayloadHandler.castSpell(spellType);
    }
}