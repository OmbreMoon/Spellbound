package com.ombremoon.spellbound.common;

import com.mojang.brigadier.CommandDispatcher;
import com.ombremoon.sentinellib.common.event.RegisterPlayerSentinelBoxEvent;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.commands.LearnSkillsCommand;
import com.ombremoon.spellbound.common.commands.LearnSpellCommand;
import com.ombremoon.spellbound.common.content.spell.ruin.SolarRaySpell;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.data.StatusHandler;
import com.ombremoon.spellbound.common.init.AttributesInit;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.events.*;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeEvents {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext context = event.getBuildContext();

        new LearnSkillsCommand(dispatcher, context);
        new LearnSpellCommand(dispatcher, context);

        ConfigCommand.register(dispatcher);
    }

    @SubscribeEvent
    public static void registerSentinelBox(RegisterPlayerSentinelBoxEvent event) {
        event.addEntry(SolarRaySpell.SOLAR_RAY);
        event.addEntry(SolarRaySpell.SOLAR_RAY_EXTENDED);
        event.addEntry(SolarRaySpell.OVERHEAT);
        event.addEntry(SolarRaySpell.SOLAR_BURST_FRONT);
        event.addEntry(SolarRaySpell.SOLAR_BURST_END);
        event.addEntry(SolarRaySpell.SOLAR_BURST_END_EXTENDED);
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.getData(DataInit.STATUS_EFFECTS).init(livingEntity);

            if (livingEntity instanceof Player player) {
                var handler = SpellUtil.getSpellHandler(player);
                handler.initData(player);
                if (!player.level().isClientSide) {
                    handler.sync();

                    var skillHandler = SpellUtil.getSkillHandler(player);
                    skillHandler.sync(player);

                    var tree = player.getData(DataInit.UPGRADE_TREE);
                    tree.refreshTree(player, tree.getUnlockedSkills());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onHandlerTick(PlayerTickEvent.Post event) {
        var handler = SpellUtil.getSpellHandler(event.getEntity());
        handler.tick();
    }

    @SubscribeEvent
    public static void onPostEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            StatusHandler status = entity.getData(DataInit.STATUS_EFFECTS);
            if (status.isInitialised()) status.tick(entity.tickCount);

            if (entity instanceof Player player) {
                if (player.tickCount % 20 == 0) {
                    double mana = player.getData(DataInit.MANA);
                    if (mana < player.getAttribute(AttributesInit.MAX_MANA).getValue()) {
                        double regen = player.getAttribute(AttributesInit.MANA_REGEN).getValue();
                        player.setData(DataInit.MANA, mana + regen);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.setData(DataInit.MANA, player.getAttribute(AttributesInit.MAX_MANA).getValue());
        PayloadHandler.syncMana(player);
    }

    @SubscribeEvent
    public static void onPlayerLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player player && player.level() instanceof ServerLevel level) {
            SpellHandler handler = SpellUtil.getSpellHandler(player);
            handler.endSpells();
        }
    }

    @SubscribeEvent
    public static void onWorldEnd(ServerStoppingEvent event) {
        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            SpellUtil.getSpellHandler(player).endSpells();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SpellUtil.getSpellHandler(event.getEntity()).endSpells();
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getSource().getEntity() instanceof Player player) {
            SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.PLAYER_KILL, new PlayerKillEvent(player, event));
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
        else if (event.getSource().getEntity() instanceof Player player)
            SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.POST_DAMAGE, new PlayerDamageEvent.Post(player, event));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.PRE_DAMAGE, new PlayerDamageEvent.Pre(player, event));
        else if (event.getSource().getEntity() instanceof Player player)
            SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.PRE_DAMAGE, new PlayerDamageEvent.Pre(player, event));
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Player player)
            SpellUtil.getSpellHandler(player).getListener().fireEvent(SpellEventListener.Events.JUMP, new PlayerJumpEvent(player, event));

    }
}
