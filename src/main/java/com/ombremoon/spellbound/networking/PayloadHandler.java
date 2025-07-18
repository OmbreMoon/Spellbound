package com.ombremoon.spellbound.networking;

import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockHolder;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.networking.clientbound.*;
import com.ombremoon.spellbound.networking.serverbound.*;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

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

    public static void equipSpell(SpellType<?> spellType, boolean equip) {
        PacketDistributor.sendToServer(new EquipSpellPayload(spellType, equip));
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

    public static void castReset(SpellType<?> spellType, boolean recast) {
        PacketDistributor.sendToServer(new CastResetPayload(spellType, recast));
    }

    public static void updateFlag(SpellType<?> spellType, int flag) {
        PacketDistributor.sendToServer(new UpdateFlagPayload(spellType, flag));
    }

    public static void setChargeOrChannel(boolean isChargingOrChanneling) {
        PacketDistributor.sendToServer(new ChargeOrChannelPayload(isChargingOrChanneling));
    }

    public static void unlockSkill(Skill skill) {
        PacketDistributor.sendToServer(new UnlockSkillPayload(skill));
    }

    public static void playAnimation(Player player, String animation) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PlayAnimationPayload(player.getUUID().toString(), animation));
    }

    public static void updateSpells(Player player, @Nullable CompoundTag spellData, boolean isRecast, int castId, boolean forceReset) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new UpdateSpellsPayload(player.getUUID().toString(), spellData, isRecast, castId, forceReset));
    }

    public static void updateSkillBuff(ServerPlayer player, SkillBuff<?> skillBuff, int duration, boolean removeBuff) {
        PacketDistributor.sendToPlayer(player, new UpdateSkillBuffPayload(player.getId(), skillBuff, duration, removeBuff));
    }

    public static void setChargeOrChannel(Player player, boolean isChargingOrChanneling) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new ChargeOrChannelPayload(isChargingOrChanneling));
    }

    public static void endSpell(Player player, SpellType<?> spellType, int castId) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new EndSpellPayload(spellType, castId));
    }

    public static void syncSpellsToClient(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncSpellPayload(SpellUtil.getSpellCaster(player).serializeNBT(player.level().registryAccess())));
    }

    public static void syncSkillsToClient(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncSkillPayload(SpellUtil.getSkills(player).serializeNBT(player.level().registryAccess())));
    }

    public static void setSpellData(Player player, SpellType<?> spellType, int id, List<SyncedSpellData.DataValue<?>> packedItems) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SetSpellDataPayload(spellType, id, packedItems));
    }

    public static void syncMana(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new ClientSyncManaPayload(player.getData(SBData.MANA)));
    }

    public static void openWorkbenchScreen(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenWorkbenchPayload());
    }

    public static void updateTree(Player player, boolean reset, List<Skill> added, Set<ResourceLocation> removed) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new UpdateTreePayload(reset, added, removed));
    }

    public static void setRotation(Entity entity, float xRot, float yRot) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new SetRotationPayload(entity.getId(), xRot, yRot));
    }

    public static void createParticles(LivingEntity entity, ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new CreateParticlesPayload(particle, x, y, z, xSpeed, ySpeed, zSpeed));
    }

    public static void addGlowEffect(Player player, int entityId) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new AddGlowEffectPayload(entityId));
    }

    public static void removeGlowEffect(ServerPlayer player, int entityId) {
        PacketDistributor.sendToPlayer(player, new RemoveGlowEffectPayload(entityId));
    }

    public static void removeFearEffect(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new RemoveFearEffectPayload());
    }

    public static void updateDimensions(MinecraftServer server, Set<ResourceKey<Level>> keys, boolean add) {
        sendToAll(server, new UpdateDimensionsPayload(keys, add));
    }

    public static void updateMultiblocks(MinecraftServer server, List<MultiblockHolder<?>> multiblocks) {
        sendToAll(server, new UpdateMultiblocksPayload(multiblocks));
    }

    public static void clearMultiblock(MinecraftServer server, CompoundTag tag) {
        sendToAll(server, new ClearMultiblockPayload(tag));
    }

/*    public static void changeHailLevel(ServerLevel level, float hailLevel) {
        PacketDistributor.sendToPlayersInDimension(level, new ChangeHailLevelPayload(hailLevel));
    }*/

    public static <PACKET extends CustomPacketPayload> void sendToAll(MinecraftServer server, PACKET packet) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.connection.hasChannel(packet)) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Constants.MOD_ID).optional();
        registrar.playToClient(
                PlayAnimationPayload.TYPE,
                PlayAnimationPayload.STREAM_CODEC,
                ClientPayloadHandler::handlePlayAnimation
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
                UpdateSkillBuffPayload.TYPE,
                UpdateSkillBuffPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientUpdateSkillBuff
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
                ClientSyncManaPayload.TYPE,
                ClientSyncManaPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientManaSync
        );
        registrar.playToClient(
                OpenWorkbenchPayload.TYPE,
                OpenWorkbenchPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClientOpenWorkbenchScreen
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
                CreateParticlesPayload.TYPE,
                CreateParticlesPayload.STREAM_CODEC,
                ClientPayloadHandler::handleCreateParticles
        );
        registrar.playToClient(
                RemoveGlowEffectPayload.TYPE,
                RemoveGlowEffectPayload.STREAM_CODEC,
                ClientPayloadHandler::handleRemoveGlowEffect
        );
        registrar.playToClient(
                RemoveFearEffectPayload.TYPE,
                RemoveFearEffectPayload.STREAM_CODEC,
                ClientPayloadHandler::handleRemoveFearEffect
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
        registrar.playToClient(
                UpdateMultiblocksPayload.TYPE,
                UpdateMultiblocksPayload.STREAM_CODEC,
                ClientPayloadHandler::handleUpdateMultiblocks
        );
        registrar.playToClient(
                ClearMultiblockPayload.TYPE,
                ClearMultiblockPayload.STREAM_CODEC,
                ClientPayloadHandler::handleClearMultiblock
        );

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
                EquipSpellPayload.TYPE,
                EquipSpellPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkEquipSpell
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
                UnlockSkillPayload.TYPE,
                UnlockSkillPayload.STREAM_CODEC,
                ServerPayloadHandler::handleNetworkUnlockSKill
        );

        registrar.playBidirectional(
                ChargeOrChannelPayload.TYPE,
                ChargeOrChannelPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientPayloadHandler::handleClientChargeOrChannel,
                        ServerPayloadHandler::handleNetworkChargeOrChannel
                )
        );
    }
}
