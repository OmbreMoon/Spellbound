package com.ombremoon.spellbound.common;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.data.StatusHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.events.PlayerDamageEvent;
import com.ombremoon.spellbound.common.magic.events.PlayerJumpEvent;
import com.ombremoon.spellbound.common.magic.events.ChangeTargetEvent;
import com.ombremoon.spellbound.common.magic.tree.SkillNode;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeEvents {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.getData(DataInit.STATUS_EFFECTS).init(livingEntity);

            if (livingEntity instanceof Player player) {
                if (!player.level().isClientSide) {
                    var handler = SpellUtil.getSpellHandler(player);
                    handler.initData(player);
                    handler.sync();

                    var skillHandler = player.getData(DataInit.SKILL_HANDLER);
//                    skillHandler.resetSkills(SpellInit.VOLCANO.get());
                    skillHandler.sync(player);

                    var tree = player.getData(DataInit.UPGRADE_TREE);
                    tree.update(player, tree.getUnlockedSkills());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPostEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            StatusHandler status = entity.getData(DataInit.STATUS_EFFECTS);
            if (status.isInitialised()) status.tick(entity.tickCount);

            if (entity instanceof Player player) {
                if (player.tickCount % 20 == 0) {
                    float mana = player.getData(DataInit.MANA);
                    if (mana < player.getData(DataInit.MAX_MANA)) player.setData(DataInit.MANA, mana+1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.setData(DataInit.MANA, player.getData(DataInit.MAX_MANA));
        PayloadHandler.syncMana(player);
        PayloadHandler.syncMaxMana(player);
    }

    @SubscribeEvent
    public static void onPlayerLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player player && player.level() instanceof ServerLevel level) {
            SpellHandler handler = SpellUtil.getSpellHandler(player);
            handler.clearAllSummons(level);
            handler.sync();
        };
    }

    @SubscribeEvent
    public static void onWorldEnd(ServerStoppingEvent event) {
        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            SpellUtil.getSpellHandler(player).clearAllSummons((ServerLevel) player.level());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            SpellUtil.getSpellHandler(event.getEntity()).getActiveSpells().clear();
            SpellHandler handler = SpellUtil.getSpellHandler(event.getEntity());
            handler.clearAllSummons(level);
            handler.sync();
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.TARGETING_EVENT, new ChangeTargetEvent(player, event));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.POST_DAMAGE, new PlayerDamageEvent.Post(player, event));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.PRE_DAMAGE, new PlayerDamageEvent.Pre(player, event));
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.JUMP, new PlayerJumpEvent(player, event));

    }
}
