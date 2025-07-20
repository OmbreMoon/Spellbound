package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.client.gui.WorkbenchScreen;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.client.KeyBinds;
import com.ombremoon.spellbound.common.magic.EffectManager;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class SpellCastEvents {

    @SubscribeEvent
    public static void onSpellMode(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled()) return;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (EffectManager.isStunned(player)) {
                event.setSwingHand(false);
                event.setCanceled(true);
                return;
            }

            var handler = SpellUtil.getSpellCaster(player);
            var spellType = handler.getSelectedSpell();

            if (spellType == null) return;

            if (handler.inCastMode()) {
                if (KeyBinds.getSpellCastMapping().isDown()) {
                    event.setSwingHand(false);
                    event.setCanceled(true);
                }
            }
        }
    }

    public static void chargeOrChannelSpell(EntityTickEvent.Post event) {
        if (!isAbleToSpellCast()) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        var handler = SpellUtil.getSpellCaster(player);
        if (!handler.inCastMode()) return;

        var spellType = handler.getSelectedSpell();
        if (spellType == null) return;

        AbstractSpell spell = handler.getCurrentlyCastSpell();
        boolean isRecast = handler.getActiveSpells(spellType).size() > 1;
        SpellContext spellContext;
        if (spell != null) {
            spellContext = spell.getCastContext();
        } else {
            spellContext = new SpellContext(spellType, player, isRecast);
            spell = spellType.createSpell();
            spell.setCastContext(spellContext);
            handler.setCurrentlyCastingSpell(spell);
            PayloadHandler.setCastingSpell(spellType, isRecast);
        }

        if (handler.castTick < 0 && !SpellUtil.canCastSpell(player, spell)) {
            spell.resetCast(handler);
        }

        boolean flag = KeyBinds.getSpellCastMapping().isDown();
        if (flag) {
            int castTime = spell.getCastTime();
            if (handler.castTick >= castTime && !handler.isChargingOrChannelling()) {
                if (spell.getCastType() == AbstractSpell.CastType.INSTANT)
                    KeyBinds.getSpellCastMapping().setDown(false);

                castSpell(player);
                handler.castTick = 0;
                handler.dirty = false;
            } else if (!handler.isChargingOrChannelling()) {
                handler.castTick++;
                if (handler.castTick == 1) {
                    spell.onCastStart(spellContext);
                    PayloadHandler.castStart(spellType, isRecast);
                }
            }
        } else if (!KeyBinds.getSpellCastMapping().isDown() && handler.castTick > 0) {
            spell.resetCast(handler);
        } else if (handler.isChargingOrChannelling()) {
            handler.setChargingOrChannelling(false);
            PayloadHandler.setChargeOrChannel(false);
        }
    }

    @SubscribeEvent
    public static void onChargeOrChannelSpell(ClientTickEvent.Post event) {
        /*if (!isAbleToSpellCast()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        var handler = SpellUtil.getSpellCaster(player);
        if (!handler.inCastMode()) return;

        var spellType = handler.getSelectedSpell();
        if (spellType == null) return;

        AbstractSpell spell = handler.getCurrentlyCastSpell();
        boolean isRecast = handler.getActiveSpells(spellType).size() > 1;
        SpellContext spellContext;
        if (spell != null) {
            spellContext = spell.getCastContext();
        } else {
            spellContext = new SpellContext(spellType, player, isRecast);
            spell = spellType.createSpell();
            spell.setCastContext(spellContext);
            handler.setCurrentlyCastingSpell(spell);
            PayloadHandler.setCastingSpell(spellType, isRecast);
        }

        if (handler.castTick < 0 && !SpellUtil.canCastSpell(player, spell)) {
            resetCast(handler, spell, spellContext, isRecast);
        }

        boolean flag = KeyBinds.getSpellCastMapping().isDown();
        if (flag) {
            int castTime = spell.getCastTime();
            if (handler.castTick >= castTime && !handler.isChargingOrChannelling()) {
                if (spell.getCastType() == AbstractSpell.CastType.INSTANT)
                    KeyBinds.getSpellCastMapping().setDown(false);

                castSpell(player);
                handler.castTick = 0;
                handler.dirty = false;
            } else if (!handler.isChargingOrChannelling()) {
                handler.castTick++;
                if (handler.castTick == 1) {
                    spell.onCastStart(spellContext);
                    PayloadHandler.castStart(spellType, isRecast);
                }

                if (handler.castTick == 0)
                    resetCast(handler, spell, spellContext, isRecast);

            }
        } else if (!KeyBinds.getSpellCastMapping().isDown() && handler.castTick > 0) {
            resetCast(handler, spell, spellContext, isRecast);
        } else if (handler.isChargingOrChannelling()) {
            handler.setChargingOrChannelling(false);
            PayloadHandler.setChargeOrChannel(false);
        }*/
    }

/*    public void resetCast(SpellHandler handler) {
        handler.castTick = 0;
        spell.onCastReset(spellContext);
        PayloadHandler.castReset(spell.getSpellType(), isRecast);
        handler.dirty = false;
    }*/

    private static boolean isAbleToSpellCast() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getOverlay() != null) return false;
        if (minecraft.screen != null) return false;
        if (!minecraft.mouseHandler.isMouseGrabbed()) return false;
        return minecraft.isWindowActive();
    }

    public static void castSpell(Player player) {
        if (player.isSpectator()) return;
        PayloadHandler.castSpell();
    }

    public static void openWorkbench() {
        Minecraft.getInstance().setScreen(new WorkbenchScreen(Component.translatable("screen.spellbound.workbench")));
    }
}
