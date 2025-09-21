package com.ombremoon.spellbound.client;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.networking.PayloadHandler;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unchecked")
public class AnimationHelper {

    public static void playAnimation(Player player, String animationName, float animationSpeed) {
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(CommonClass.customLocation("animation"));
        if (animation != null) {
            animation.addModifier(new SpeedModifier(animationSpeed), 0);
            animation.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(2, Ease.INOUTSINE), new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(CommonClass.customLocation(animationName))), true);
        }
    }

    public static void stopAnimation(Player player, String animationName) {
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(CommonClass.customLocation("animation"));
        if (animation == null) return;

        var layer = ((KeyframeAnimationPlayer)animation.getAnimation());
        if (layer != null && layer.getData().getName().equals(animationName))
            layer.stop();
    }

    public static void tick() {
        Player player = Minecraft.getInstance().player;
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(CommonClass.customLocation("animation"));
        if (animation != null) {
            if (!animation.isActive() && animation.size() > 0) {
                for (int i = 0; i < animation.size(); i++) {
                    animation.removeModifier(i);
                }
            } else if (animation.isActive()) {
                player.setYBodyRot(player.getYHeadRot());
                PayloadHandler.updateRotation(player.yBodyRot);
            }
        }
    }
}
