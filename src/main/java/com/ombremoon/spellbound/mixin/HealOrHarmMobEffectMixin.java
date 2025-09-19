package com.ombremoon.spellbound.mixin;

import com.ombremoon.spellbound.common.init.SBData;
import net.minecraft.world.effect.HealOrHarmMobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(HealOrHarmMobEffect.class)
public class HealOrHarmMobEffectMixin {

    @Shadow @Final public boolean isHarm;

    @Inject(method = "applyInstantenousEffect", at = @At("HEAD"))
    private void applyInstantaneousEffectPre(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity livingEntity, int amplifier, double health, CallbackInfo info) {
        if (!livingEntity.level().isClientSide && this.isHarm == livingEntity.isInvertedHealAndHarm() && indirectSource instanceof Player player) {
            livingEntity.setData(SBData.EFFECT_HEAL_TARGET, player.getId());
        }
    }

    @Inject(method = "applyInstantenousEffect", at = @At("TAIL"))
    private void applyInstantaneousEffectPost(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity livingEntity, int amplifier, double health, CallbackInfo info) {
        if (!livingEntity.level().isClientSide && this.isHarm == livingEntity.isInvertedHealAndHarm()) {
            livingEntity.removeData(SBData.EFFECT_HEAL_TARGET);
        }
    }
}
