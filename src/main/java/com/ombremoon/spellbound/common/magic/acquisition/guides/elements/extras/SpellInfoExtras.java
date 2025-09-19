package com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpellInfoExtras(int colour, int lineGap, boolean dropShadow, boolean mastery, int baseDamage, int castTime, int duration, int manaCost, int manaPerTick) {
    /**
     * For baseDamage down to manaPerTick the integer value indicates how it should be displayed
     * 0 Never display
     * 1 Display if value is > 0
     * 2 Always display
     */
    public static final Codec<SpellInfoExtras> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("colour", 0).forGetter(SpellInfoExtras::colour),
            Codec.INT.optionalFieldOf("lineGap", 9).forGetter(SpellInfoExtras::lineGap),
            Codec.BOOL.optionalFieldOf("dropShadow", false).forGetter(SpellInfoExtras::dropShadow),
            Codec.BOOL.optionalFieldOf("mastery", true).forGetter(SpellInfoExtras::mastery),
            Codec.INT.optionalFieldOf("baseDamage", 1).forGetter(SpellInfoExtras::baseDamage),
            Codec.INT.optionalFieldOf("castTime", 1).forGetter(SpellInfoExtras::castTime),
            Codec.INT.optionalFieldOf("duration", 1).forGetter(SpellInfoExtras::duration),
            Codec.INT.optionalFieldOf("manaCost", 1).forGetter(SpellInfoExtras::manaCost),
            Codec.INT.optionalFieldOf("manaPerTick", 1).forGetter(SpellInfoExtras::manaPerTick)
    ).apply(inst, SpellInfoExtras::new));

    public static SpellInfoExtras getDefault() {
        return new SpellInfoExtras(0, 9, false, true, 1, 1, 1, 1, 1);
    }
}
