package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.client.KeyBinds;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
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
            if (player.hasEffect(EffectInit.STUNNED)) {
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

        AbstractSpell spell = spellType.createSpell();
        boolean isRecast = handler.getActiveSpells(spellType).size() > 1;
        var spellContext = new SpellContext(player, spell.getTargetEntity(player, 10), isRecast);
        if (KeyBinds.getSpellCastMapping().isDown()) {
            int castTime = spell.getCastTime();
            if (handler.castTick >= castTime && !handler.isChannelling()) {
                if (spell.getCastType() != AbstractSpell.CastType.CHANNEL) KeyBinds.getSpellCastMapping().setDown(false);
                castSpell(player, spellType);
                handler.castTick = 0;
            } else if (!handler.isChannelling()){
                handler.castTick++;
                if (handler.castTick == 1) {
                    spell.onCastStart(spellContext);
                    PayloadHandler.castStart(spellType, isRecast);
                } else {
                    spell.whenCasting(spellContext, handler.castTick);
                    PayloadHandler.whenCasting(spellType, handler.castTick, isRecast);
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
    }

    private static boolean isAbleToSpellCast() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getOverlay() != null) return false;
        if (minecraft.screen != null) return false;
        if (!minecraft.mouseHandler.isMouseGrabbed()) return false;
        return minecraft.isWindowActive();
    }

    @SuppressWarnings("unchecked")
    public static void castSpell(Player player, SpellType<?> spellType) {
        AbstractSpell spell = spellType.createSpell();
        if (player.isSpectator()) return;
        if (spell == null) return;
        if (!SpellUtil.canCastSpell(player, spell)) return;

        PayloadHandler.castSpell(spellType);
        spell.initSpell(player, player.level(), player.getOnPos());
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(CommonClass.customLocation("animation"));
        if (animation != null)
            animation.setAnimation(new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(CommonClass.customLocation("test"))));
    }
}
