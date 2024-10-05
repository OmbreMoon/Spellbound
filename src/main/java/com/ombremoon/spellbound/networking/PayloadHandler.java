package com.ombremoon.spellbound.networking;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.networking.clientbound.*;
import com.ombremoon.spellbound.networking.serverbound.*;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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

    public static void castSpell(SpellType<?> spellType) {
        PacketDistributor.sendToServer(new CastSpellPayload(spellType));
    }

    public static void cycleSpell() {
        PacketDistributor.sendToServer(new CycleSpellPayload());
    }

    public static void whenCasting(SpellType<?> spellType, int castTime, boolean recast) {
        PacketDistributor.sendToServer(new CastingPayload(spellType, castTime, recast));
    }

    public static void stopChannel() {
        PacketDistributor.sendToServer(new StopChannelPayload());
    }

    public static void unlockSkill(Skill skill) {
        PacketDistributor.sendToServer(new UnlockSkillPayload(skill));
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
                        SpellUtil.getSkillHandler(player)
                                .serializeNBT(player.level().registryAccess())
                ));
    }

    public static void syncMana(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player,
                new ClientSyncManaPayload(player.getData(DataInit.MANA)));
    }

    public static void openWorkbenchScreen(Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenWorkbenchPayload());
    }

    public static void updateTree(Player player, boolean reset, List<Skill> added, Set<ResourceLocation> removed) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new UpdateTreePayload(reset, added, removed));
    }

    public static void shakeScreen(Player player, int duration, float intensity, float maxOffset, int freq) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new ShakeScreenPayload(duration, intensity, maxOffset, freq));
    }

    public static void setRotation(Player player, float xRot, float yRot) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SetRotationPayload(xRot, yRot));
    }

    public static void removeAfterglow(Player player, int entityId) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, new RemoveAfterglowPayload(entityId));
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SwitchModePayload.TYPE,
                SwitchModePayload.CODEC,
                ServerPayloadHandler::handleNetworkSwitchMode
        );
        registrar.playToServer(
                CastSpellPayload.TYPE,
                CastSpellPayload.CODEC,
                ServerPayloadHandler::handleNetworkCastSpell
        );
        registrar.playToServer(
                CycleSpellPayload.TYPE,
                CycleSpellPayload.CODEC,
                ServerPayloadHandler::handleNetworkCycleSpell
        );
        registrar.playToServer(
                CastingPayload.TYPE,
                CastingPayload.CODEC,
                ServerPayloadHandler::handleNetworkCasting
        );
        registrar.playToServer(
                StopChannelPayload.TYPE,
                StopChannelPayload.CODEC,
                ServerPayloadHandler::handleNetworkStopChannel
        );
        registrar.playToServer(
                UnlockSkillPayload.TYPE,
                UnlockSkillPayload.CODEC,
                ServerPayloadHandler::handleNetworkUnlockSKill
        );

        registrar.playToClient(
                SyncSpellPayload.TYPE,
                SyncSpellPayload.CODEC,
                ClientPayloadHandler::handleClientSpellSync
        );
        registrar.playToClient(
                SyncSkillPayload.TYPE,
                SyncSkillPayload.CODEC,
                ClientPayloadHandler::handleClientSkillSync
        );
        registrar.playToClient(
                OpenWorkbenchPayload.TYPE,
                OpenWorkbenchPayload.CODEC,
                ClientPayloadHandler::handleClientOpenWorkbenchScreen
        );
        registrar.playToClient(
                ClientSyncManaPayload.TYPE,
                ClientSyncManaPayload.CODEC,
                ClientPayloadHandler::handleClientManaSync
        );
        registrar.playToClient(
                UpdateTreePayload.TYPE,
                UpdateTreePayload.CODEC,
                ClientPayloadHandler::handleClientUpdateTree
        );
        registrar.playToClient(
                ShakeScreenPayload.TYPE,
                ShakeScreenPayload.CODEC,
                ClientPayloadHandler::handleClientShakeScreen
        );
        registrar.playToClient(
                SetRotationPayload.TYPE,
                SetRotationPayload.CODEC,
                ClientPayloadHandler::handleClientSetRotation
        );
        registrar.playToClient(
                RemoveAfterglowPayload.TYPE,
                RemoveAfterglowPayload.CODEC,
                ClientPayloadHandler::handleRemoveAfterglow
        );
    }
}
