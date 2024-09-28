package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.effects.SBEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EffectInit {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister
            .create(Registries.MOB_EFFECT, Constants.MOD_ID);

    public static final Holder<MobEffect> INFLAMED = EFFECTS.register("inflamed", () -> new SBEffect(MobEffectCategory.HARMFUL, 8889187));
    public static final Holder<MobEffect> FROZEN = EFFECTS.register("frozen", () -> new SBEffect(MobEffectCategory.HARMFUL, 8889187));
    public static final Holder<MobEffect> SHOCKED = EFFECTS.register("shocked", () -> new SBEffect(MobEffectCategory.HARMFUL, 8889187));
    public static final Holder<MobEffect> POISON = EFFECTS.register("poison", () -> new SBEffect(MobEffectCategory.HARMFUL, 8889187));
    public static final Holder<MobEffect> WIND = EFFECTS.register("wind", () -> new SBEffect(MobEffectCategory.HARMFUL, 8889187));
    public static final Holder<MobEffect> EARTH = EFFECTS.register("earth", () -> new SBEffect(MobEffectCategory.HARMFUL, 8889187));

    public static final Holder<MobEffect> HEALING_TOUCH = EFFECTS.register("healing_touch", () -> new SBEffect(MobEffectCategory.BENEFICIAL, 8889187));

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
