package com.ombremoon.spellbound.common.content;

public interface HailstormData {

    boolean isHailing();

    void setHailing(boolean hailing);

    float getHailLevel(float delta);

    void setHailLevel(float strength);

    void prepareHail();
}
