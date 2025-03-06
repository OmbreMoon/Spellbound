package com.ombremoon.spellbound.common.magic;

public enum SpellMastery {
    NOVICE(0),
    APPRENTICE(20),
    ADEPT(40),
    EXPERT(60),
    MASTER(80);

    private final int levelRequirement;

    SpellMastery(int levelRequirement) {
        this.levelRequirement = levelRequirement;
    }

    public int getLevelRequirement() {
        return this.levelRequirement;
    }
}
