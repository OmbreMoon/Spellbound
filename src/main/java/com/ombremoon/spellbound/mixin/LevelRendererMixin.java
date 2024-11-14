package com.ombremoon.spellbound.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.Spellbound;
import com.ombremoon.spellbound.client.ClientStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
