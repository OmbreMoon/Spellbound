package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.client.KeyBinds;
import com.ombremoon.spellbound.common.EffectManager;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

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

            var handler = SpellUtil.getSpellHandler(player);
            var spellType = handler.getSelectedSpell();

            if (spellType == null) return;

            if (handler.inCastMode()) {
                if (KeyBinds.getSpellCastMapping().isDown()) {
                    if (player.isCrouching()) {
                        SpellUtil.cycle(handler, spellType);
                        PayloadHandler.cycleSpell(handler.getSelectedSpell());
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
        if (!isAbleToSpellCast()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        var handler = SpellUtil.getSpellHandler(player);
        if (!handler.inCastMode()) return;

        var spellType = handler.getSelectedSpell();
        if (spellType == null) return;

        AbstractSpell spell = handler.getCurrentlyCastSpell();
        boolean isRecast = handler.getActiveSpells(spellType).size() > 1;
        SpellContext spellContext;
        if (spell != null) {
            spellContext = spell.getCastContext();
        } else {
            spellContext = new SpellContext(player, isRecast);
            spell = spellType.createSpell();
            spell.setCastContext(spellContext);
            handler.setCurrentlyCastingSpell(spell);
            PayloadHandler.setCastingSpell(spellType, isRecast);
        }

        boolean flag = KeyBinds.getSpellCastMapping().isDown();
        if (flag) {
            int castTime = spell.getCastTime();
            if (handler.castTick >= castTime && !handler.isChannelling()) {
                if (spell.getCastType() == AbstractSpell.CastType.INSTANT) KeyBinds.getSpellCastMapping().setDown(false);
                castSpell(player, spell);
                handler.castTick = 0;
            } else if (!handler.isChannelling()){
                handler.castTick++;
                if (handler.castTick > 1) {
                    spell.whenCasting(spellContext, handler.castTick);
                    PayloadHandler.whenCasting(spellType, handler.castTick, isRecast);
                } else {
                    spell.onCastStart(spellContext);
                    PayloadHandler.castStart(spellType, isRecast);
                }
            }
        } else if (!KeyBinds.getSpellCastMapping().isDown() && handler.castTick > 0) {
            handler.castTick = 0;
            spell.onCastReset(spellContext);
            PayloadHandler.castReset(spellType, isRecast);
        } else if (handler.isChannelling()) {
            handler.setChannelling(false);
            PayloadHandler.stopChannel();
        }

        handler.castKeyDown = flag;
        PayloadHandler.setCastKey(flag);
    }

    private static boolean isAbleToSpellCast() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getOverlay() != null) return false;
        if (minecraft.screen != null) return false;
        if (!minecraft.mouseHandler.isMouseGrabbed()) return false;
        return minecraft.isWindowActive();
    }

    @SuppressWarnings("unchecked")
    public static void castSpell(Player player, AbstractSpell spell) {
        if (player.isSpectator()) return;
        if (!SpellUtil.canCastSpell(player, spell)) return;

        PayloadHandler.castSpell();
        spell.initSpell(player, player.level(), player.getOnPos());
        var handler = SpellUtil.getSpellHandler(player);
        handler.setCurrentlyCastingSpell(null);
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(CommonClass.customLocation("animation"));
        if (animation != null)
            animation.setAnimation(new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(CommonClass.customLocation("test"))));
    }
}
