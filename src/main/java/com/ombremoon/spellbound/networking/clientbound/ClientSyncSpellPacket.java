package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.capability.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientSyncSpellPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientSyncSpellPacket> TYPE =
            new CustomPacketPayload.Type<>(CommonClass.customLocation("client_spell_sync"));

    public static final StreamCodec<ByteBuf, ClientSyncSpellPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ClientSyncSpellPacket::tag,
            ClientSyncSpellPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(ClientSyncSpellPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            SpellHandler handler = new SpellHandler();
            handler.deserializeNBT(context.player().level().registryAccess(), packet.tag());
            context.player().setData(DataInit.SPELL_HANDLER, handler);
        });
    }
}
