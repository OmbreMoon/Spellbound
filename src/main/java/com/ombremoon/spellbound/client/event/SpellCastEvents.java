package com.ombremoon.spellbound.client.event;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.client.CameraEngine;
import com.ombremoon.spellbound.client.KeyBinds;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
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
        if (!isAbleToSpellCast()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        var handler = SpellUtil.getSpellHandler(player);
        if (!handler.inCastMode()) return;

        var spellType = handler.getSelectedSpell();
        if (spellType == null) return;

        if (KeyBinds.getSpellCastMapping().isDown()) {
            AbstractSpell spell = spellType.createSpell();
            int castTime = spell.getCastTime();
            if (handler.castTick >= castTime && !handler.isChannelling()) {
                if (spell.getCastType() != AbstractSpell.CastType.CHANNEL) KeyBinds.getSpellCastMapping().setDown(false);
                castSpell(player, spellType);
                handler.castTick = 0;
            } else if (!handler.isChannelling()){
                handler.castTick++;
                PayloadHandler.whenCasting(spellType, handler.castTick);
            }
        } else if (handler.isChannelling()) {
            handler.setChannelling(false);
            PayloadHandler.stopChannel();
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

    @SuppressWarnings("unchecked")
    public static void castSpell(Player player, SpellType<?> spellType) {
        AbstractSpell spell = spellType.createSpell();
        if (player.isSpectator()) return;
        if (spell == null) return;
        if (!SpellUtil.canCastSpell(player, spell)) return;

        PayloadHandler.castSpell(spellType);
        CameraEngine cameraEngine = CameraEngine.getOrAssignEngine(player);
        cameraEngine.shakeScreen(player.getRandom().nextInt(), 10, 1, 100);
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(CommonClass.customLocation("animation"));
        if (animation != null)
            animation.setAnimation(new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(CommonClass.customLocation("waving"))));
    }
}
