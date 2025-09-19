package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.client.AnimationHelper;
import com.ombremoon.spellbound.client.gui.WorkbenchScreen;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.api.SpellType;
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
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        AnimationHelper.tick();
        var handler = SpellUtil.getSpellCaster(player);

        if (!isAbleToSpellCast()) return;
        if (!handler.inCastMode()) return;

        var spellType = handler.getSelectedSpell();
        if (spellType == null) return;

        AbstractSpell spell = handler.getCurrentlyCastSpell();
        if (spell != null) {
            if (handler.castTick > 0 && !SpellUtil.canCastSpell(player, spell)) {
                SpellContext spellContext = createContext(player, handler, spell);
                spell.resetCast(handler, spellContext);
            }

            boolean flag = KeyBinds.getSpellCastMapping().isDown();
            if (flag) {
                int castTime = spell.getCastTime();
                if (handler.castTick >= castTime && !handler.isChargingOrChannelling()) {
                    if (spell.getCastType() == AbstractSpell.CastType.INSTANT)
                        KeyBinds.getSpellCastMapping().setDown(false);

                    castSpell(player);
                    handler.castTick = 0;
                } else if (!handler.isChargingOrChannelling()) {
                    handler.castTick++;
                    if (handler.castTick == 1) {
                        SpellContext spellContext = createContext(player, handler, spell);
                        spell.onCastStart(spellContext);
                        PayloadHandler.castStart();
                    }
                }
            } else if (!KeyBinds.getSpellCastMapping().isDown() && handler.castTick > 0) {
                spell.resetCast(handler);
            } else if (handler.isChargingOrChannelling()) {
                handler.setChargingOrChannelling(false);
                PayloadHandler.setChargeOrChannel(false);
            }
        } else {
            spell = spellType.createSpell();
            handler.setCurrentlyCastingSpell(spell);
            createContext(player, handler, spell);
        }
    }

    public static SpellContext createContext(Player player, SpellHandler handler, AbstractSpell spell) {
        SpellType<?> spellType = spell.spellType();
        boolean isRecast = handler.getActiveSpells(spellType).size() > 1;
        Entity target = spell.getTargetEntity(player, SpellUtil.getCastRange(player));
        target = target == null || target.isRemoved() ? null : target;
        SpellContext context = new SpellContext(spellType, player, target, isRecast);
        spell.setCastContext(context);
        PayloadHandler.setCastingSpell(spellType, context);
        return context;
    }

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
}
