package com.ombremoon.spellbound.common.magic;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum SpellPath implements StringRepresentable {
    RUIN(0x7C0A02, false, "ruin", null),
    TRANSFIGURATION(0x32CD32, false, "transfiguration", null),
    SUMMONS(0x055C9D, false, "summons", null),
    DIVINE(0xD4AF37, false, "divine", null),
    DECEPTION(0x541675, false, "deception", null),
    FIRE(0xD73502, true, "fire", EffectManager.Effect.FIRE),
    FROST(0x4F9CC8, true, "frost", EffectManager.Effect.FROST),
    SHOCK(0x9543C9, true, "shock", EffectManager.Effect.SHOCK);

    private final int color;
    private final boolean isSubPath;
    private final String name;
    @Nullable
    private final EffectManager.Effect effect;

    SpellPath(int color, boolean isSubPath, String name, @Nullable EffectManager.Effect effect) {
        this.color = color;
        this.isSubPath = isSubPath;
        this.name = name;
        this.effect = effect;
    }

    public int getColor() {
        return this.color;
    }

    public boolean isSubPath() {
        return this.isSubPath;
    }

    public @Nullable EffectManager.Effect getEffect() {
        return this.effect;
    }

    public static SpellPath getPathById(int ordinal) {
        return SpellPath.values()[ordinal];
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
