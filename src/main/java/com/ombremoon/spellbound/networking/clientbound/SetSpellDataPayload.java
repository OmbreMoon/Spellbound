package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SetSpellDataPayload(SpellType<?> spellType, int id, List<SyncedSpellData.DataValue<?>> packedItems) implements CustomPacketPayload {
    public static final Type<SetSpellDataPayload> TYPE = new Type<>(CommonClass.customLocation("set_spell_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetSpellDataPayload> STREAM_CODEC = StreamCodec.ofMember(
            SetSpellDataPayload::write, SetSpellDataPayload::new
    );

    private SetSpellDataPayload(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(getSpellType(registryFriendlyByteBuf.readUtf()), registryFriendlyByteBuf.readVarInt(), unpack(registryFriendlyByteBuf));
    }

    private static SpellType<?> getSpellType(String location) {
        return SBSpells.SPELL_TYPES.getRegistry().get().get(ResourceLocation.tryParse(location));
    }

    private static void pack(List<SyncedSpellData.DataValue<?>> dataValues, RegistryFriendlyByteBuf buffer) {
        for (SyncedSpellData.DataValue<?> datavalue : dataValues) {
            datavalue.write(buffer);
        }

        buffer.writeByte(255);
    }

    private static List<SyncedSpellData.DataValue<?>> unpack(RegistryFriendlyByteBuf buffer) {
        List<SyncedSpellData.DataValue<?>> list = new ArrayList<>();

        int i;
        while ((i = buffer.readUnsignedByte()) != 255) {
            list.add(SyncedSpellData.DataValue.read(buffer, i));
        }

        return list;
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(this.spellType.toString());
        buffer.writeVarInt(this.id);
        pack(this.packedItems, buffer);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
