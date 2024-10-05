package com.ombremoon.spellbound.mixin;

import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(method = "getRenderType", at = @At("HEAD"))
    private void getRenderType(T livingEntity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        Player player = Minecraft.getInstance().player;
        var handler = SpellUtil.getSpellHandler(player);
        if (handler.hasAfterGlow(livingEntity) && livingEntity.hasEffect(EffectInit.AFTERGLOW)) {
            cir.setReturnValue(RenderType.outline(self().getTextureLocation(livingEntity)));
        }
    }

    private LivingEntityRenderer<T, M> self() {
        return (LivingEntityRenderer<T, M>) (Object) this;
    }
}
