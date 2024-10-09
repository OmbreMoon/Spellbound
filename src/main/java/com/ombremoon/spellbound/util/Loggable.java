package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.Constants;

public interface Loggable {

    default void log(Object o) {
        Constants.LOG.info("{}", o);
    }
}
