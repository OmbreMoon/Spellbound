package com.ombremoon.spellbound.common.magic.sync;

import java.util.List;

public interface SpellDataHolder {
    void onSpellDataUpdated(SpellDataKey<?> dataKey);

    void onSpellDataUpdated(List<SyncedSpellData.DataValue<?>> newData);
}
