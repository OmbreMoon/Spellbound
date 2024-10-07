package com.ombremoon.spellbound.common.content.effects;

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

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect) {
        this(causeEntity, effect, 0);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration) {
        this(causeEntity, effect, duration, 0);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration, int amplifier) {
        this(causeEntity, effect, duration, amplifier, false, true);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible) {
        this(causeEntity, effect, duration, amplifier, ambient, visible, visible);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon) {
        this(causeEntity, effect, duration, amplifier, ambient, visible, showIcon, null);
    }

    public SBEffectInstance(LivingEntity causeEntity, Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon, @Nullable MobEffectInstance hiddenEffect) {
        super(effect, duration, amplifier, ambient, visible, showIcon, hiddenEffect);
        this.causeEntity = causeEntity;
    }

    public LivingEntity getCauseEntity() {
        return this.causeEntity;
    }

    @Override
    public void onEffectAdded(LivingEntity livingEntity) {
        super.onEffectAdded(livingEntity);
        if (this.causeEntity instanceof Player player) {
            if (this.getEffect().equals(EffectInit.AFTERGLOW)) {
                PayloadHandler.addGlowEffect(player, livingEntity.getId());
            }
        }
    }
}
