package com.ombremoon.spellbound.common.events;

import com.mojang.brigadier.CommandDispatcher;
import com.ombremoon.sentinellib.common.event.RegisterPlayerSentinelBoxEvent;
import com.ombremoon.spellbound.common.content.commands.LearnSkillsCommand;
import com.ombremoon.spellbound.common.content.commands.LearnSpellCommand;
import com.ombremoon.spellbound.common.content.commands.SpellboundCommand;
import com.ombremoon.spellbound.common.content.entity.spell.Hail;
import com.ombremoon.spellbound.common.content.spell.ruin.fire.SolarRaySpell;
import com.ombremoon.spellbound.common.content.world.dimension.DimensionCreator;
import com.ombremoon.spellbound.common.content.world.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.content.world.hailstorm.HailstormData;
import com.ombremoon.spellbound.common.content.world.hailstorm.HailstormSavedData;
import com.ombremoon.spellbound.common.events.custom.MobEffectEvent;
import com.ombremoon.spellbound.common.events.custom.TickChunkEvent;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.buff.SpellModifier;
import com.ombremoon.spellbound.common.magic.api.buff.events.*;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.common.magic.EffectManager;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;

import java.util.List;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeEvents {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext context = event.getBuildContext();

        new LearnSkillsCommand(dispatcher, context);
        new LearnSpellCommand(dispatcher, context);
        new SpellboundCommand(dispatcher, context);

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
            var handler = SpellUtil.getSpellHandler(livingEntity);
            handler.initData(livingEntity);

            if (livingEntity instanceof Player player) {
                if (!player.level().isClientSide) {
                    handler.sync();

                    var holder = SpellUtil.getSkillHolder(player);
                    holder.sync();

                    var tree = player.getData(SBData.UPGRADE_TREE);
                    tree.update(player, tree.getUnlockedSkills());

                    Level level = player.level();
                    BlockPos blockPos = handler.getLastArenaPosition();
                    if (handler.isArenaOwner(handler.getLastArenaEntered()) && blockPos != null && !level.dimension().location().getNamespace().equals(Constants.MOD_ID)) {
                        blockPos = blockPos.offset(-4, 0, -4);

                        for (int i = 0; i < 5; i++) {
                            for (int j = 0; j < 5; j++) {
                                BlockPos blockPos1 = blockPos.offset(i, 0, j);
                                BlockState blockState = level.getBlockState(blockPos1);
                                if (blockState.is(SBBlocks.SUMMON_STONE.get()) || blockState.is(SBBlocks.SUMMON_PORTAL.get()))
                                    level.setBlock(blockPos1, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                        handler.setLastArenaPosition(null);
                        handler.closeArena(handler.getLastArenaEntered());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveDimension(EntityLeaveLevelEvent event) {
        Level level = event.getLevel();
        Entity entity = event.getEntity();
        if (!level.isClientSide && entity instanceof Player player) {
            var handler = SpellUtil.getSpellHandler(player);
            if (handler.isArenaOwner(handler.getLastArenaEntered()) && level.dimension().location().getNamespace().equals(Constants.MOD_ID))
                DimensionCreator.get().markDimensionForUnregistration(level.getServer(), level.dimension());
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
            EffectManager status = entity.getData(SBData.STATUS_EFFECTS);
            if (status.isInitialised()) status.tick(entity.tickCount);

            if (entity instanceof Player player) {
                if (player.tickCount % 20 == 0) {
                    double mana = player.getData(SBData.MANA);
                    if (mana < player.getAttribute(SBAttributes.MAX_MANA).getValue()) {
                        double regen = player.getAttribute(SBAttributes.MANA_REGEN).getValue();
                        player.setData(SBData.MANA, mana + regen);
                    }
                }
            }

            if (entity.hasEffect(SBEffects.STUNNED) || entity.hasEffect(SBEffects.ROOTED))
                entity.setDeltaMovement(0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity livingEntity = event.getEntity();
        if (event.getEffect().is(SBEffects.FEAR)) {
            var skills = SpellUtil.getSkillHolder(livingEntity);
            skills.removeModifier(SpellModifier.FEAR);
            livingEntity.setData(SBData.FEAR_TICK, 0);
            livingEntity.setData(SBData.FEAR_SOURCE, Vec3.ZERO);
            if (livingEntity instanceof ServerPlayer player)
                PayloadHandler.removeFearEffect(player);
        }

        if (event.getEffectInstance() instanceof SBEffectInstance effectInstance && effectInstance.willGlow()) {
            LivingEntity entity = effectInstance.getCauseEntity();
            if (entity instanceof ServerPlayer player)
                PayloadHandler.removeGlowEffect(player, livingEntity.getId());
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        HailstormData data = HailstormSavedData.get(event.getLevel());
        data.prepareHail();
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        HailstormData data = HailstormSavedData.get(level);
        if (level.dimension() == Level.OVERWORLD) {
            if (!level.isClientSide) {
                ServerLevel serverLevel = (ServerLevel) level;
                HailstormSavedData savedData = (HailstormSavedData) data;
                if (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                    int i = ((ServerLevelData) level.getLevelData()).getClearWeatherTime();
                    int j = ((ServerLevelData) level.getLevelData()).getRainTime();
                    int k = savedData.getHailTime();
                    boolean flag = savedData.isHailing();
                    if (i > 0) {
                        k = flag ? 0 : 1;
                        flag = false;
                    } else {
                        if (k > 0) {
                            if (--k == 0)
                                flag = !flag;
                        } else if (flag) {
                            k = 0;
                        } else {
                            k = 0;
                        }
                    }

                    savedData.setHailTime(k);
                    savedData.setHailing(flag);

                }

                savedData.tickHailLevel(serverLevel);
                int sleepTime = level.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
                if (serverLevel.sleepStatus.areEnoughSleeping(sleepTime) && serverLevel.sleepStatus.areEnoughDeepSleeping(sleepTime, serverLevel.players())) {
                    if (serverLevel.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && savedData.isHailing()) {
                        savedData.setHailTime(0);
                        savedData.setHailing(false);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onChunkTick(TickChunkEvent.Pre event) {
        LevelChunk chunk = event.getChunk();
        Level level = chunk.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            ChunkPos chunkpos = chunk.getPos();
            HailstormSavedData data = (HailstormSavedData) HailstormSavedData.get(serverLevel);
            boolean flag = data.isHailing();
            int i = chunkpos.getMinBlockX();
            int j = chunkpos.getMinBlockZ();
            BlockPos blockpos = data.findLightningTargetAround(serverLevel, serverLevel.getBlockRandomPos(i, 0, j, 15));
            if (flag) {
                if (data.isHailingAt(serverLevel, blockpos) && data.chunkHasCyclone(serverLevel, blockpos)) {
                    if (serverLevel.random.nextInt(100) == 0) {
                        LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                        if (lightningbolt != null) {
                            lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos));
                            serverLevel.addFreshEntity(lightningbolt);
                        }
                    }

                    if (serverLevel.random.nextInt(100) == 0) {
                        Hail hail = SBEntities.HAIL.get().create(serverLevel);
                        if (hail != null) {
                            hail.moveTo(Vec3.atBottomCenterOf(blockpos.atY(serverLevel.getMaxBuildHeight())));
                            serverLevel.addFreshEntity(hail);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.setData(SBData.MANA, player.getAttribute(SBAttributes.MAX_MANA).getValue());
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

        if (event.getSource().getEntity() instanceof LivingEntity livingEntity) {
            SpellUtil.getSpellHandler(livingEntity).getListener().fireEvent(SpellEventListener.Events.PLAYER_KILL, new DeathEvent(livingEntity, event));
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide) return;

        SpellUtil.getSpellHandler(event.getEntity()).getListener().fireEvent(SpellEventListener.Events.CHANGE_TARGET, new ChangeTargetEvent(event.getEntity(), event));
    }

    @SubscribeEvent
    public static void onLivingAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide) return;

        SpellUtil.getSpellHandler(event.getEntity()).getListener().fireEvent(SpellEventListener.Events.ATTACK, new PlayerAttackEvent(event.getEntity(), event));
    }

    @SubscribeEvent
    public static void onLivingBlock(LivingShieldBlockEvent event) {
        if (event.getEntity().level().isClientSide) return;

        SpellUtil.getSpellHandler(event.getEntity()).getListener().fireEvent(SpellEventListener.Events.BLOCK, new BlockEvent(event.getEntity(), event));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;

        SpellUtil.getSpellHandler(event.getEntity()).getListener().fireEvent(SpellEventListener.Events.POST_DAMAGE, new DamageEvent.Post(event.getEntity(), event));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level().isClientSide) return;

        SpellUtil.getSpellHandler(event.getEntity()).getListener().fireEvent(SpellEventListener.Events.PRE_DAMAGE, new DamageEvent.Pre(livingEntity, event));

        if (livingEntity.hasEffect(SBEffects.SLEEP))
            livingEntity.removeEffect(SBEffects.SLEEP);
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().isClientSide) return;

        SpellUtil.getSpellHandler(event.getEntity()).getListener().fireEvent(SpellEventListener.Events.JUMP, new JumpEvent(event.getEntity(), event));

    }
}
