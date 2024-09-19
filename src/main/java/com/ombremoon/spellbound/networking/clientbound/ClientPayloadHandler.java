package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.client.CameraEngine;
import com.ombremoon.spellbound.client.gui.WorkbenchScreen;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleClientSpellSync(SyncSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            SpellHandler handler = new SpellHandler();
            handler.deserializeNBT(context.player().level().registryAccess(), payload.tag());
            handler.caster = context.player();
            context.player().setData(DataInit.SPELL_HANDLER, handler);
        });
    }

    public static void handleClientSkillSync(SyncSkillPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            SkillHandler handler = new SkillHandler();
            handler.deserializeNBT(context.player().level().registryAccess(), payload.tag());
            context.player().setData(DataInit.SKILL_HANDLER, handler);
        });
    }

    public static void handleClientOpenWorkbenchScreen(OpenWorkbenchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new WorkbenchScreen(Component.translatable("screen.spellbound.workbench")));
        });
    }

    public static void handleClientShakeScreen(ShakeScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CameraEngine cameraEngine = CameraEngine.getOrAssignEngine(context.player());
            cameraEngine.shakeScreen(context.player().getRandom().nextInt(), payload.duration(), payload.intensity(), payload.maxOffset(), payload.freq());
        });
    }
}
