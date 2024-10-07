package com.ombremoon.spellbound.common.magic.sync;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataTypeInit;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.ClassTreeIdRegistry;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class SyncedSpellData {
    public static Logger LOGGER = Constants.LOG;
    static final ClassTreeIdRegistry ID_REGISTRY = new ClassTreeIdRegistry();
    private final SpellDataHolder spell;
    private final DataItem<?>[] dataItems;
    private boolean isDirty;

    public SyncedSpellData(SpellDataHolder spell, DataItem<?>[] dataItems) {
        this.spell = spell;
        this.dataItems = dataItems;
    }

    public static <T> SpellDataKey<T> define(Class<? extends SpellDataHolder> clazz, SpellDataType<T> dataType) {
        if (true || LOGGER.isDebugEnabled()) { // Forge: This is very useful for mods that register keys on classes that are not their own
            try {
                Class<?> oclass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
                if (!oclass.equals(clazz)) {
                    // Forge: log at warn, mods should not add to classes that they don't own, and only add stacktrace when in debug is enabled as it is mostly not needed and consumes time
                    if (LOGGER.isDebugEnabled()) LOGGER.warn("defineId called for: {} from {}", clazz, oclass, new RuntimeException());
                    else LOGGER.warn("defineId called for: {} from {}", clazz, oclass);
                }
            } catch (ClassNotFoundException classnotfoundexception) {
            }
        }

        int i = ID_REGISTRY.define(clazz);
        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
        } else {
            return dataType.createDataKey(i);
        }
    }

    private <T> DataItem<T> getItem(SpellDataKey<T> key) {
        return (DataItem<T>)this.dataItems[key.id()];
    }

    public <T> T get(SpellDataKey<T> key) {
        return this.getItem(key).getValue();
    }

    public <T> void set(SpellDataKey<T> key, T value) {
        this.set(key, value, false);
    }

    public <T> void set(SpellDataKey<T> key, T value, boolean force) {
        DataItem<T> dataitem = this.getItem(key);
        if (force || ObjectUtils.notEqual(value, dataitem.getValue())) {
            dataitem.setValue(value);
            this.spell.onSpellDataUpdated(key);
            dataitem.setDirty(true);
            this.isDirty = true;
        }
    }


    public boolean isDirty() {
        return this.isDirty;
    }

    @Nullable
    public List<DataValue<?>> packDirty() {
        if (!this.isDirty) {
            return null;
        } else {
            this.isDirty = false;
            List<DataValue<?>> list = new ArrayList<>();

            for (DataItem<?> dataitem : this.dataItems) {
                if (dataitem.isDirty()) {
                    dataitem.setDirty(false);
                    list.add(dataitem.value());
                }
            }

            return list;
        }
    }

    @Nullable
    public List<DataValue<?>> getNonDefaultValues() {
        List<DataValue<?>> list = null;

        for (DataItem<?> dataitem : this.dataItems) {
            if (!dataitem.isDefault()) {
                if (list == null) {
                    list = new ArrayList<>();
                }

                list.add(dataitem.value());
            }
        }

        return list;
    }

    public void assignValues(List<DataValue<?>> entries) {
        for (DataValue<?> dataValue : entries) {
            DataItem<?> dataItem = this.dataItems[dataValue.id];
            this.assignValue(dataItem, dataValue);
            this.spell.onSpellDataUpdated(dataItem.getDataKey());
        }

        this.spell.onSpellDataUpdated(entries);
    }

    private <T> void assignValue(DataItem<T> target, DataValue<?> entry) {
        if (!Objects.equals(entry.dataType(), target.dataKey.dataType())) {
            throw new IllegalStateException(
                    String.format(Locale.ROOT,"Invalid spell data item type for field %d on spell %s: old=%s(%s), new =%s(%s)",
                    target.dataKey.id(), this.spell, target.value, target.value.getClass(), entry.value, entry.value.getClass()));
        } else {
            target.setValue((T) entry.value);
        }
    }

    public static class Builder {
        private final SpellDataHolder spell;
        private final DataItem<?>[] dataItems;

        public Builder(SpellDataHolder spell) {
            this.spell = spell;
            this.dataItems = new DataItem[SyncedSpellData.ID_REGISTRY.getCount(spell.getClass())];

        }

        public <T> SyncedSpellData.Builder define(SpellDataKey<T> key, T value) {
            int i = key.id();
            if (i > this.dataItems.length) {
                throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + this.dataItems.length + ")");
            } else if (this.dataItems[i] != null) {
                throw new IllegalArgumentException("Duplicate id value for " + i + "!");
            } else if (DataTypeInit.getDataTypeId(key.dataType()) < 0) {
                throw new IllegalArgumentException("Unregistered data type " + key.dataType() + " for " + i + "!");
            } else {
                this.dataItems[key.id()] = new DataItem<>(key, value);
                return this;
            }
        }

        public SyncedSpellData build() {
            for (int i = 0; i < this.dataItems.length; i++) {
                if (this.dataItems[i] == null)
                    throw new IllegalStateException("Spell " + this.spell.getClass() + "has not defined synced data value " + i);
            }
            return new SyncedSpellData(this.spell, this.dataItems);
        }
    }

    public static class DataItem<T> {
        final SpellDataKey<T> dataKey;
        T value;
        private final T initialValue;
        private boolean dirty;

        public DataItem(SpellDataKey<T> dataKey, T value) {
            this.dataKey = dataKey;
            this.initialValue = value;
            this.value = value;
        }

        public SpellDataKey<T> getDataKey() {
            return this.dataKey;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        public boolean isDefault() {
            return this.initialValue.equals(this.value);
        }

        public SyncedSpellData.DataValue<T> value() {
            return DataValue.create(this.dataKey, this.value);
        }
    }

    public static record DataValue<T>(int id, SpellDataType<T> dataType, T value) {
        public static <T> DataValue<T> create(SpellDataKey<T> dataKey, T value) {
            SpellDataType<T> spellDataType = dataKey.dataType();
            return new DataValue<>(dataKey.id(), spellDataType, spellDataType.copy(value));
        }

        public void write(RegistryFriendlyByteBuf buffer) {
            int i = DataTypeInit.getDataTypeId(this.dataType);
            if (i < 0) {
                throw new EncoderException("Unknown data type type " + this.dataType);
            } else {
                buffer.writeByte(this.id);
                buffer.writeVarInt(i);
                this.dataType.codec().encode(buffer, this.value);
            }
        }

        public static DataValue<?> read(RegistryFriendlyByteBuf buffer, int id) {
            int i = buffer.readVarInt();
            SpellDataType<?> spellDataType = DataTypeInit.getDataType(i);
            if (spellDataType == null) {
                throw new DecoderException("Unknown data type type " + i);
            } else {
                return read(buffer, id, spellDataType);
            }
        }

        private static <T> DataValue<T> read(RegistryFriendlyByteBuf buffer, int id, SpellDataType<T> dataType) {
            return new DataValue<>(id, dataType, dataType.codec().decode(buffer));
        }
    }
}
