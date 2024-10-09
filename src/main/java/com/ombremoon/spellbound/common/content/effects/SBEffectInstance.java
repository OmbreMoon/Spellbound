package com.ombremoon.spellbound.common.content.effects;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SBEffectInstance extends MobEffectInstance {
    private final LivingEntity causeEntity;
    private final boolean willGlow;

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect) {
        this(causeEntity, effect, 0);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration) {
        this(causeEntity, effect, duration, false);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration, boolean willGlow) {
        this(causeEntity, effect, duration, willGlow, 0);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration, boolean willGlow, int amplifier) {
        this(causeEntity, effect, duration, willGlow, amplifier, false, true);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration, boolean willGlow, int amplifier, boolean ambient, boolean visible) {
        this(causeEntity, effect, duration, willGlow, amplifier, ambient, visible, visible);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration, boolean willGlow, int amplifier, boolean ambient, boolean visible, boolean showIcon) {
        this(causeEntity, effect, duration, willGlow, amplifier, ambient, visible, showIcon, null);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration, boolean willGlow, int amplifier, boolean ambient, boolean visible, boolean showIcon, @Nullable MobEffectInstance hiddenEffect) {
        super(effect, duration, amplifier, ambient, visible, showIcon, hiddenEffect);
        this.causeEntity = causeEntity;
        this.willGlow = willGlow;
    }

    public LivingEntity getCauseEntity() {
        return this.causeEntity;
    }

    public boolean willGlow() {
        return this.willGlow;
    }

    @Override
    public boolean tick(LivingEntity entity, Runnable onExpirationRunnable) {
//        Constants.LOG.info("{}", this.getDuration());
        return super.tick(entity, onExpirationRunnable);
    }

    @Override
    public void onEffectAdded(LivingEntity livingEntity) {
        super.onEffectAdded(livingEntity);
        if (!livingEntity.level().isClientSide) {
            if (this.causeEntity instanceof Player player && !this.causeEntity.is(livingEntity) && this.willGlow) {
                PayloadHandler.addGlowEffect(player, livingEntity.getId());
            }
        }
    }
}