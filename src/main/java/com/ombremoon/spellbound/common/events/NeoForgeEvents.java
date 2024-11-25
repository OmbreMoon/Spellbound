package com.ombremoon.spellbound.common.events;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.ombremoon.sentinellib.common.event.RegisterPlayerSentinelBoxEvent;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.EffectManager;
import com.ombremoon.spellbound.common.commands.LearnSkillsCommand;
import com.ombremoon.spellbound.common.commands.LearnSpellCommand;
import com.ombremoon.spellbound.common.content.entity.spell.Hail;
import com.ombremoon.spellbound.common.content.world.HailstormData;
import com.ombremoon.spellbound.common.content.world.HailstormSavedData;
import com.ombremoon.spellbound.common.content.spell.ruin.fire.SolarRaySpell;
import com.ombremoon.spellbound.common.events.custom.BuildSpellEvent;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.buff.events.*;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
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
            livingEntity.getData(SBData.STATUS_EFFECTS).init(livingEntity);
            var handler = SpellUtil.getSpellHandler(livingEntity);
            handler.initData(livingEntity);

            if (livingEntity instanceof Player player) {
                if (!player.level().isClientSide) {
                    handler.sync();

                    var holder = SpellUtil.getSkillHolder(player);
                    holder.sync();

                    var tree = player.getData(SBData.UPGRADE_TREE);
                    tree.update(player, tree.getUnlockedSkills());
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
                    int i = ((ServerLevelData)level.getLevelData()).getClearWeatherTime();
                    int j = ((ServerLevelData)level.getLevelData()).getRainTime();
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

                savedData.setDirty();

                ServerChunkCache chunkCache = ((ServerChunkCache)level.getChunkSource());
                List<ServerChunkCache.ChunkAndHolder> list = Lists.newArrayListWithCapacity(chunkCache.chunkMap.size());

                for (ChunkHolder chunkholder : chunkCache.chunkMap.getChunks()) {
                    LevelChunk levelchunk = chunkholder.getTickingChunk();
                    if (levelchunk != null) {
                        list.add(new ServerChunkCache.ChunkAndHolder(levelchunk, chunkholder));
                    }
                }

                for (ServerChunkCache.ChunkAndHolder serverchunkcache$chunkandholder : list) {
                    LevelChunk levelchunk1 = serverchunkcache$chunkandholder.chunk();
                    ChunkPos chunkpos = levelchunk1.getPos();
                    if (chunkCache.level.shouldTickBlocksAt(chunkpos.toLong())) {
                        boolean flag = data.isHailing();
                        int i = chunkpos.getMinBlockX();
                        int j = chunkpos.getMinBlockZ();
                        BlockPos blockpos = savedData.findLightningTargetAround(serverLevel, level.getBlockRandomPos(i, 0, j, 15));
                        if (flag) {
                            if (savedData.isHailingAt(level, blockpos) && savedData.chunkHasCyclone(serverLevel, blockpos)) {
                                if (level.random.nextInt(100) == 0) {
                                    LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(level);
                                    if (lightningbolt != null) {
                                        lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos));
                                        level.addFreshEntity(lightningbolt);
                                    }
                                }

                                if (level.random.nextInt(100) == 0) {
                                    Hail hail = SBEntities.HAIL.get().create(level);
                                    if (hail != null) {
                                        hail.moveTo(Vec3.atBottomCenterOf(blockpos.atY(level.getMaxBuildHeight())));
                                        level.addFreshEntity(hail);
                                    }
                                }
                            }
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
        if (event.getEntity().level().isClientSide) return;

        SpellUtil.getSpellHandler(event.getEntity()).getListener().fireEvent(SpellEventListener.Events.PRE_DAMAGE, new DamageEvent.Pre(event.getEntity(), event));
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().isClientSide) return;

        SpellUtil.getSpellHandler(event.getEntity()).getListener().fireEvent(SpellEventListener.Events.JUMP, new JumpEvent(event.getEntity(), event));

    }
}