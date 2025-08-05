package com.ombremoon.spellbound.mixin;

import net.minecraft.client.renderer.blockentity.StructureBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureBlockRenderer.class)
public class StructureBlockRendererMixin {

    @Inject(method = "getViewDistance", at = @At("RETURN"), cancellable = true)
    private void getViewDistance(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(256);
    }

}
