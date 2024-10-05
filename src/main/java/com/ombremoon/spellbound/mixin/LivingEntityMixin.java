package com.ombremoon.spellbound.mixin;

import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onEffectRemoved", at = @At("TAIL"))
    private void onEffectRemoved(MobEffectInstance effectInstance, CallbackInfo info) {
        if (effectInstance instanceof SBEffectInstance instance && effectInstance.getEffect().is(EffectInit.AFTERGLOW)) {
            var handler = SpellUtil.getSpellHandler(instance.getCauseEntity());
            handler.removeAfterglow(self());
        }
    }

    private LivingEntity self() {
        return (LivingEntity) (Object) this;
    }
}
