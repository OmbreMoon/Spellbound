package com.ombremoon.spellbound.common.magic.acquisition.divine.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.Mth;

public record HealPredicate(MinMaxBounds.Doubles health) {
    public static final Codec<HealPredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("health", MinMaxBounds.Doubles.ANY).forGetter(HealPredicate::health)
            ).apply(instance, HealPredicate::new)
    );

    public static HealPredicate healed(MinMaxBounds.Doubles healAmount) {
        return new HealPredicate(healAmount);
    }

    public boolean matches(double startHealth, double endHeath) {
        float f = (float) (startHealth - endHeath);
        return this.health.matches(Mth.abs(f));
    }
}
