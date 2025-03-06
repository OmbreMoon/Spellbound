package com.ombremoon.spellbound.mixin;

import com.ombremoon.spellbound.common.events.EventFactory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onEffectRemoved", at = @At(value = "TAIL"), cancellable = true)
    private void onEffectRemoved(MobEffectInstance instance, CallbackInfo info) {
        if (EventFactory.onEffectRemoved(spellbound$self(), instance)) info.cancel();
    }

    @Unique
    private LivingEntity spellbound$self() {
        return (LivingEntity) (Object) this;
    }
}
