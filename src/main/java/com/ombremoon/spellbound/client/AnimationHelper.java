package com.ombremoon.spellbound.client;

import com.ombremoon.spellbound.main.CommonClass;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

@SuppressWarnings("unchecked")
public class AnimationHelper {

    public static void playAnimation(Player player, String animationName) {
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(CommonClass.customLocation("animation"));
        if (animation != null) {
            animation.addModifier(new AdjustmentModifier(partName -> {
                float rotationX = 0;
                float rotationY = 0;
                float rotationZ = 0;
                float scaleX = 0;
                float scaleY = 0;
                float scaleZ = 0;
                float offsetX = 0;
                float offsetY = 0;
                float offsetZ = 0;

                rotationY = 0;

                return Optional.of(new AdjustmentModifier.PartModifier(
                        new Vec3f(rotationX, rotationY, rotationZ),
                        new Vec3f(scaleX, scaleY, scaleZ),
                        new Vec3f(offsetX, offsetY, offsetZ)
                ));
            }), 0);
//            animation.addModifier(new SpeedModifier(2.0F), 1);
            animation.setAnimation(new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(CommonClass.customLocation(animationName))));
        }
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
            }
        }
    }
}
