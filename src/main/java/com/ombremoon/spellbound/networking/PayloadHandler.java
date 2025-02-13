package com.ombremoon.spellbound.networking;

import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.networking.clientbound.*;
import com.ombremoon.spellbound.networking.serverbound.*;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadHandler {

    public static void switchMode() {
        PacketDistributor.sendToServer(new SwitchModePayload());
    }

    public static void setSpell(SpellType<?> spellType) {
        PacketDistributor.sendToServer(new SetSpellPayload(spellType));
    }

    public static void castSpell() {
        PacketDistributor.sendToServer(new CastSpellPayload());
    }

    public static void setCastingSpell(SpellType<?> spellType, boolean isRecast) {
        PacketDistributor.sendToServer(new SetCastingSpellPayload(spellType, isRecast));
    }

    public static void castStart(SpellType<?> spellType, boolean recast) {
        PacketDistributor.sendToServer(new CastStartPayload(spellType, recast));
    }

    public static void whenCasting(SpellType<?> spellType, int castTime, boolean recast) {
        PacketDistributor.sendToServer(new CastingPayload(spellType, castTime, recast));
    }

    public static void castReset(SpellType<?> spellType, boolean recast) {
        PacketDistributor.sendToServer(new CastResetPayload(spellType, recast));
    }

    public static void updateFlag(SpellType<?> spellType, int flag) {
        PacketDistributor.sendToServer(new UpdateFlagPayload(spellType, flag));
    }

    public static void setCastKey(boolean isDown) {
        PacketDistributor.sendToServer(new SetCastKeyPayload(isDown));
    }

    public static void stopChannel() {
        PacketDistributor.sendToServer(new StopChannelPayload());
    }

    public static void unlockSkill(Skill skill) {
        PacketDistributor.sendToServer(new UnlockSkillPayload(skill));
    }

    public static void updateSpells(Player player, boolean isRecast, int castId, boolean forceReset) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new UpdateSpellsPayload(isRecast, castId, forceReset));
    }

    public static void endSpell(Player player, SpellType<?> spellType, int castId) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new EndSpellPayload(spellType, castId));
    }

    public static void syncSpellsToClient(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player,
                new SyncSpellPayload(
                        SpellUtil.getSpellHandler(player)
                                .serializeNBT(player.level().registryAccess())
                ));
    }

    public static void syncSkillsToClient(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player,
                new SyncSkillPayload(
                        SpellUtil.getSkillHolder(player)
                                .serializeNBT(player.level().registryAccess())
                ));
    }

    public static void setSpellData(Player player, SpellType<?> spellType, int id, List<SyncedSpellData.DataValue<?>> packedItems) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SetSpellDataPayload(spellType, id, packedItems));
    }

    public static void syncMana(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player,
                new ClientSyncManaPayload(player.getData(SBData.MANA)));
    }

    public static void openWorkbenchScreen(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenWorkbenchPayload());
    }

    public static void updateTree(Player player, boolean reset, List<Skill> added, Set<ResourceLocation> removed) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new UpdateTreePayload(reset, added, removed));
    }

    public static void setRotation(Player player, float xRot, float yRot) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SetRotationPayload(xRot, yRot));
    }

    public static void addGlowEffect(Player player, int entityId) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new AddGlowEffectPayload(entityId));
    }

    public static void removeGlowEffect(Player player, int entityId) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new RemoveGlowEffectPayload(entityId));
    }

    public static void updateDimensions(MinecraftServer server, Set<ResourceKey<Level>> keys, boolean add) {
        sendToAll(server, new UpdateDimensionsPayload(keys, add));
    }

    public static void changeHailLevel(ServerLevel level, float hailLevel) {
        PacketDistributor.sendToPlayersInDimension(level, new ChangeHailLevelPayload(hailLevel));
    }

    public static <PACKET extends CustomPacketPayload> void sendToAll(MinecraftServer server, PACKET packet) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.connection.hasChannel(packet)) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SwitchModePayload.TYPE,
                SwitchModePayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkSwitchMode
        );
        registrar.playToServer(
                CastSpellPayload.TYPE,
                CastSpellPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkCastSpell
        );
        registrar.playToServer(
                SetSpellPayload.TYPE,
                SetSpellPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkSetSpell
        );
        registrar.playToServer(
                SetCastingSpellPayload.TYPE,
                SetCastingSpellPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkSetCastSpell
        );
        registrar.playToServer(
                CastStartPayload.TYPE,
                CastStartPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkCastStart
        );
        registrar.playToServer(
                CastingPayload.TYPE,
                CastingPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkCasting
        );
        registrar.playToServer(
                CastResetPayload.TYPE,
                CastResetPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkCastReset
        );
        registrar.playToServer(
                UpdateFlagPayload.TYPE,
                UpdateFlagPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkUpdateFlag
        );
        registrar.playToServer(
                SetCastKeyPayload.TYPE,
                SetCastKeyPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkSetCastKey
        );
        registrar.playToServer(
                StopChannelPayload.TYPE,
                StopChannelPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkStopChannel
        );
        registrar.playToServer(
                UnlockSkillPayload.TYPE,
                UnlockSkillPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkUnlockSKill
        );

        registrar.playToClient(
                EndSpellPayload.TYPE,
                EndSpellPayload.STREAM_CODEC,
                ClientPayloadHandler::handleEndSpell
        );
        registrar.playToClient(
                UpdateSpellsPayload.TYPE,
                UpdateSpellsPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientUpdateSpells
        );
        registrar.playToClient(
                SyncSpellPayload.TYPE,
                SyncSpellPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientSpellSync
        );
        registrar.playToClient(
                SyncSkillPayload.TYPE,
                SyncSkillPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientSkillSync
        );
        registrar.playToClient(
                SetSpellDataPayload.TYPE,
                SetSpellDataPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientSetSpellData
        );
        registrar.playToClient(
                OpenWorkbenchPayload.TYPE,
                OpenWorkbenchPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientOpenWorkbenchScreen
        );
        registrar.playToClient(
                ClientSyncManaPayload.TYPE,
                ClientSyncManaPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientManaSync
        );
        registrar.playToClient(
                UpdateTreePayload.TYPE,
                UpdateTreePayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientUpdateTree
        );
        registrar.playToClient(
                SetRotationPayload.TYPE,
                SetRotationPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientSetRotation
        );
        registrar.playToClient(
                RemoveGlowEffectPayload.TYPE,
                RemoveGlowEffectPayload.STREAM_CODEC,
                ClientPayloadHandler::handleRemoveGlowEffect
        );
        registrar.playToClient(
                AddGlowEffectPayload.TYPE,
                AddGlowEffectPayload.STREAM_CODEC,
                ClientPayloadHandler::handleAddGlowEffect
        );
        registrar.playToClient(
                ChangeHailLevelPayload.TYPE,
                ChangeHailLevelPayload.STREAM_CODEC,
                ClientPayloadHandler::handleChangeHailLevel
        );
        registrar.playToClient(
                UpdateDimensionsPayload.TYPE,
                UpdateDimensionsPayload.STREAM_CODEC,
                ClientPayloadHandler::handleUpdateDimensions
        );
    }
}
