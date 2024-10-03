package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.client.CameraEngine;
import com.ombremoon.spellbound.client.gui.WorkbenchScreen;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.tree.UpgradeTree;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleClientSpellSync(SyncSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            SpellHandler handler = SpellUtil.getSpellHandler(context.player());
            handler.deserializeNBT(context.player().level().registryAccess(), payload.tag());
            if (handler.caster == null) handler.caster = context.player();
        });
    }

    public static void handleClientManaSync(ClientSyncManaPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().setData(DataInit.MANA, payload.mana());
        });
    }

    public static void handleClientSkillSync(SyncSkillPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var handler = SpellUtil.getSkillHandler(context.player());
            handler.deserializeNBT(context.player().level().registryAccess(), payload.tag());
        });
    }

    public static void handleClientOpenWorkbenchScreen(OpenWorkbenchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new WorkbenchScreen(Component.translatable("screen.spellbound.workbench")));
        });
    }

    public static void handleClientUpdateTree(UpdateTreePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().getData(DataInit.UPGRADE_TREE).update(payload);
        });
    }

    public static void handleClientShakeScreen(ShakeScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CameraEngine cameraEngine = CameraEngine.getOrAssignEngine(context.player());
            cameraEngine.shakeScreen(context.player().getRandom().nextInt(), payload.duration(), payload.intensity(), payload.maxOffset(), payload.freq());
        });
    }
}
