package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.Constants;

/**
 * Utility interface used to make objects easier to debug.
 */
public interface Loggable {

    default void log(Object o) {
        Constants.LOG.info("{}", o);
    }
}
