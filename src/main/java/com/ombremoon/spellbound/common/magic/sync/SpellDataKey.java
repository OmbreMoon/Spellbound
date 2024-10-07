package com.ombremoon.spellbound.common.magic.sync;

public record SpellDataKey<T>(int id, SpellDataType<T> dataType) {
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            SpellDataKey<?> dataKey = (SpellDataKey<?>)other;
            return this.id == dataKey.id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return "<spell data: " + this.id + ">";
    }
}
