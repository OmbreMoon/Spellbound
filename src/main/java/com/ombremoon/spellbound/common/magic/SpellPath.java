package com.ombremoon.spellbound.common.magic;

public enum SpellPath {
    RUIN(0x7C0A02, false),
    TRANSFIGURATION(0x1E4620, false),
    SUMMONS(0x000080, false),
    DIVINE(0xD4AF37, false),
    DECEPTION(0x541675, false),
    FIRE(0x541675, true),
    FROST(0x541675, true),
    SHOCK(0x541675, true);

    private final int color;
    private final boolean isSubPath;

    SpellPath(int color, boolean isSubPath) {
        this.color = color;
        this.isSubPath = isSubPath;
    }

    public int getColor() {
        return this.color;
    }

    public boolean isSubPath() {
        return this.isSubPath;
    }

    public static SpellPath getPathById(int ordinal) {
        return SpellPath.values()[ordinal];
    }
}
