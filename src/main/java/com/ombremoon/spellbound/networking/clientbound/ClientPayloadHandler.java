package com.ombremoon.spellbound.networking.clientbound;

import com.ombremoon.spellbound.client.AnimationHelper;
import com.ombremoon.spellbound.client.event.SpellCastEvents;
import com.ombremoon.spellbound.common.content.world.hailstorm.HailstormData;
import com.ombremoon.spellbound.common.content.world.hailstorm.HailstormSavedData;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockManager;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.buff.SpellModifier;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.networking.serverbound.ChargeOrChannelPayload;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientPayloadHandler {

    public static void handlePlayAnimation(PlayAnimationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player().level().getPlayerByUUID(UUID.fromString(payload.playerId()));
            if (player != null)
                AnimationHelper.playAnimation(player, payload.animation());
        });
    }

    public static void handleEndSpell(EndSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var handler = SpellUtil.getSpellCaster(context.player());
            AbstractSpell spell = handler.getSpell(payload.spellType(), payload.castId());
            if (spell != null)
                spell.endSpell();
        });
    }

    public static void handleClientChargeOrChannel(final ChargeOrChannelPayload payload, final IPayloadContext context) {
        var handler = SpellUtil.getSpellCaster(context.player());
        handler.setChargingOrChannelling(payload.isChargingOrChannelling());
    }

    public static void handleClientUpdateSpells(UpdateSpellsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();

            Player player = level.getPlayerByUUID(UUID.fromString(payload.playerId()));
            if (player != null) {
                var caster = SpellUtil.getSpellCaster(player);
                AbstractSpell spell = caster.getCurrentlyCastSpell();
                if (spell != null)
                    spell.clientInitSpell(player, level,player.getOnPos(), payload.spellData(), payload.isRecast(), payload.castId(), payload.forceReset(), payload.shiftSpells());

//                caster.setCurrentlyCastingSpell(null);
            }
        });
    }

    public static void handleClientUpdateSpellTicks(UpdateSpellTicksPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();

            Entity entity = level.getEntity(payload.entityId());
            if (entity instanceof LivingEntity livingEntity) {
                var caster = SpellUtil.getSpellCaster(livingEntity);
                AbstractSpell spell = caster.getSpell(payload.spellType(), payload.castId());
                if (spell != null)
                    spell.tickCount = payload.ticks();
            }
        });
    }

    public static void handleClientUpdateSkillBuff(UpdateSkillBuffPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();

            Entity entity = level.getEntity(payload.entityId());
            if (entity instanceof LivingEntity livingEntity) {
                var caster = SpellUtil.getSpellCaster(livingEntity);
                if (!payload.removeBuff()) {
                    caster.forceAddBuff(payload.skillBuff(), payload.duration());
                } else {
                    caster.removeSkillBuff(payload.skillBuff());
                }
            }
        });
    }

    public static void handleClientUpdateCooldowns(UpdateCooldownsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();

            Entity entity = level.getEntity(payload.entityId());
            if (entity instanceof LivingEntity livingEntity) {
                var skills = SpellUtil.getSkills(livingEntity);
                skills.getCooldowns().addCooldown(payload.skill(), payload.duration());
            }
        });
    }

    public static void handleClientSpellSync(SyncSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();

            SpellHandler handler = SpellUtil.getSpellCaster(context.player());
            handler.deserializeNBT(level.registryAccess(), payload.tag());
            if (handler.caster == null) handler.caster = context.player();
        });
    }

    public static void handleClientManaSync(SyncManaPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().setData(SBData.MANA, payload.mana());
        });
    }

    public static void handleClientSkillSync(SyncSkillPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();

            var holder = SpellUtil.getSkills(context.player());
            holder.deserializeNBT(level.registryAccess(), payload.tag());
        });
    }

    public static void handleClientSetSpellData(SetSpellDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            Entity entity = level.getEntity(payload.entityId());
            if (entity instanceof LivingEntity livingEntity) {
                var handler = SpellUtil.getSpellCaster(livingEntity);
                AbstractSpell spell = handler.castTick > 0 ? handler.getCurrentlyCastSpell() : handler.getSpell(payload.spellType(), payload.id());
                if (spell != null)
                    spell.getSpellData().assignValues(payload.packedItems());
            }
        });
    }

    public static void handleClientOpenWorkbenchScreen(OpenWorkbenchPayload payload, IPayloadContext context) {
        context.enqueueWork(SpellCastEvents::openWorkbench);
    }

    public static void handleClientUpdateTree(UpdateTreePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().getData(SBData.UPGRADE_TREE).update(payload);
        });
    }

    public static void handleClientSetRotation(SetRotationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (entity != null) {
                entity.setXRot(payload.xRot());
                entity.setYRot(payload.yRot());
                entity.setYHeadRot(payload.yRot());
                entity.setYBodyRot(payload.yRot());
            }
        });
    }

    public static void handleCreateParticles(CreateParticlesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            level.addParticle(payload.particle(), payload.x(), payload.y(), payload.z(), payload.xSpeed(), payload.ySpeed(), payload.zSpeed());
        });
    }

    public static void handleAddGlowEffect(AddGlowEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            var handler = SpellUtil.getSpellCaster(context.player());
            Entity entity = level.getEntity(payload.entityId());
            if (entity instanceof LivingEntity livingEntity)
                handler.addGlowEffect(livingEntity);
        });
    }

    public static void handleRemoveGlowEffect(RemoveGlowEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            var handler = SpellUtil.getSpellCaster(context.player());
            Entity entity = level.getEntity(payload.entityId());
            if (entity instanceof LivingEntity livingEntity)
                handler.removeGlowEffect(livingEntity);
        });
    }

    public static void handleRemoveFearEffect(RemoveFearEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            var skills = SpellUtil.getSkills(player);
            player.setData(SBData.FEAR_TICK, 0);
            skills.removeModifier(SpellModifier.FEAR);
        });
    }

    public static void handleChangeHailLevel(ChangeHailLevelPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            HailstormData data = HailstormSavedData.get(level);
            data.setHailLevel(payload.hailLevel());
        });
    }

    public static void handleUpdateMultiblocks(UpdateMultiblocksPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            MultiblockManager multiblockManager = MultiblockManager.getInstance(context.player().level());
            multiblockManager.updateMultiblocks(payload.multiblocks());
            Constants.LOG.info("Loaded {} multiblocks on the client", payload.multiblocks().size());
        });
    }

    public static void handleUpdateDimensions(UpdateDimensionsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            @SuppressWarnings("resource")
            var player = context.player();

            if (player instanceof LocalPlayer localPlayer) {
                final Set<ResourceKey<Level>> dimensionList = localPlayer.connection.levels();
                Consumer<ResourceKey<Level>> keyConsumer = payload.add() ? dimensionList::add : dimensionList::remove;
                payload.keys().forEach(keyConsumer);
            }
        });
    }
}
