package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.Constants;

/**
 * Utility interface used to make objects easier to debug.
 */
public interface Loggable {

    default void log(Object o) {
        Constants.LOG.info("{}", o);
    }
    default void warn(Object o) {
        Constants.LOG.warn("{}", o);
    }

    default void error(Object o) {
        Constants.LOG.error("{}", o);
    }
}
