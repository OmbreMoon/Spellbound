package com.ombremoon.spellbound.common.capability;

public interface ISpellHandler {

    boolean inCastMode();

    void switchMode(boolean castMode);
}
