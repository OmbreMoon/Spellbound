package com.ombremoon.spellbound.mixin;

import com.ombremoon.spellbound.client.shader.SBShaders;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "resize", at = @At("HEAD"))
    private void resize(int width, int height, CallbackInfo info) {
        SBShaders.resize(width, height);
    }
}
