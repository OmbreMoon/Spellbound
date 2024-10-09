package com.ombremoon.spellbound.mixin;

import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.networking.PayloadHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onEffectRemoved", at = @At(value = "TAIL"))
    private void onEffectRemoved(MobEffectInstance instance, CallbackInfo info) {
        if (!spellbound$self().level().isClientSide) {
            if (instance instanceof SBEffectInstance effectInstance && effectInstance.willGlow()) {
                LivingEntity entity = effectInstance.getCauseEntity();
                if (entity instanceof Player player)
                    PayloadHandler.removeGlowEffect(player, spellbound$self().getId());
            }
        }
    }

    @Unique
    private LivingEntity spellbound$self() {
        return (LivingEntity) (Object) this;
    }
}
