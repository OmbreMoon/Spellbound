package com.ombremoon.spellbound.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    /*@ModifyVariable(method = "renderEntity", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private MultiBufferSource modifyBufferSource(MultiBufferSource original, Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack poseStack) {
        if (entity.distanceToSqr(Minecraft.getInstance().player.position()) < 100) {
            return ClientStuff.getInstance().getExamples().exampleBufferSource();
        }
        return original;
    }*/
}
