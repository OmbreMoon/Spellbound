package com.ombremoon.spellbound.common.magic;

public enum SpellPath {
    RUIN(0x7C0A02),
    DECEPTION(0x541675),
    DIVINE(0xD4AF37),
    TRANSFIGURATION(0x1E4620),
    SUMMONS(0x000080);

    private final int color;

    SpellPath(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }
}
