package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.client.gui.WorkbenchScreen;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
            context.player().setData(SBData.MANA, payload.mana());
        });
    }

    public static void handleClientSkillSync(SyncSkillPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var holder = SpellUtil.getSkillHolder(context.player());
            holder.deserializeNBT(context.player().level().registryAccess(), payload.tag());
        });
    }

    public static void handleClientSetSpellData(SetSpellDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var handler = SpellUtil.getSpellHandler(context.player());
            AbstractSpell spell = handler.castTick > 0 ? handler.getCurrentlyCastSpell() : handler.getSpell(payload.spellType(), payload.id());
            if (spell != null)
                spell.getSpellData().assignValues(payload.packedItems());
        });
    }

    public static void handleClientOpenWorkbenchScreen(OpenWorkbenchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new WorkbenchScreen(Component.translatable("screen.spellbound.workbench")));
        });
    }

    public static void handleClientUpdateTree(UpdateTreePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().getData(SBData.UPGRADE_TREE).update(payload);
        });
    }

    public static void handleClientSetRotation(SetRotationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().setXRot(payload.xRot());
            context.player().setYRot(payload.yRot());
        });
    }

    public static void handleAddGlowEffect(AddGlowEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var handler = SpellUtil.getSpellHandler(context.player());
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (entity instanceof LivingEntity livingEntity)
                handler.addGlowEffect(livingEntity);
        });
    }

    public static void handleRemoveGlowEffect(RemoveGlowEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var handler = SpellUtil.getSpellHandler(context.player());
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (entity instanceof LivingEntity livingEntity)
                handler.removeGlowEffect(livingEntity);
        });
    }
}
