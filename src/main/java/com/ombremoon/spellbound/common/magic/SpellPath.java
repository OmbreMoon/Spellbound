package com.ombremoon.spellbound.common.magic;

import net.minecraft.util.StringRepresentable;

public enum SpellPath implements StringRepresentable {
    RUIN(0x7C0A02, false, "ruin"),
    TRANSFIGURATION(0x32CD32, false, "transfiguration"),
    SUMMONS(0x000080, false, "summons"),
    DIVINE(0xD4AF37, false, "divine"),
    DECEPTION(0x541675, false, "deception"),
    FIRE(0xD73502, true, "fire"),
    FROST(0x4F9CC8, true, "frost"),
    SHOCK(0x9543C9, true, "shock");

    private final int color;
    private final boolean isSubPath;
    private final String name;

    SpellPath(int color, boolean isSubPath, String name) {
        this.color = color;
        this.isSubPath = isSubPath;
        this.name = name;
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

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
