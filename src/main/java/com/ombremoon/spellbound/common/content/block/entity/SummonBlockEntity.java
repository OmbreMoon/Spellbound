package com.ombremoon.spellbound.common.content.block.entity;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.world.dimension.DynamicDimensionFactory;
import com.ombremoon.spellbound.common.content.world.dimension.TestDimensionFactory;
import com.ombremoon.spellbound.common.init.SBBlockEntities;
import com.ombremoon.spellbound.common.magic.acquisition.ArenaSavedData;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.slf4j.Logger;

import java.util.UUID;

public class SummonBlockEntity extends BlockEntity {
    protected static final Logger LOGGER = Constants.LOG;
    private UUID owner;
    private int arenaId;
    private BlockPos frontTopLeft;
    private ResourceLocation spell;
    private boolean hasEnteredPortal;

    protected SummonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public SummonBlockEntity(BlockPos pos, BlockState blockState) {
        this(SBBlockEntities.SUMMON_PORTAL.get(), pos, blockState);
    }

    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.level().isClientSide || entity.getServer() == null)
            return;

        if (entity.isOnPortalCooldown())
            return;

        boolean canTeleportInDimension = entity.level().dimension() == Level.NETHER || entity.level().dimension() == Level.OVERWORLD;
        if (entity instanceof LivingEntity livingEntity && canTeleportInDimension && livingEntity.canUsePortal(false)
                && Shapes.joinIsNotEmpty(
                Shapes.create(entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ())),
                state.getShape(level, pos),
                BooleanOp.AND
        )) {
            MinecraftServer server = level.getServer();
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SummonBlockEntity) {
                ArenaSavedData data = ArenaSavedData.get(server);
                ResourceKey<Level> levelKey = data.getOrCreateKey(server, this.arenaId);
                ServerLevel arena = TestDimensionFactory.createDimension(server, levelKey);
                if (arena != null && this.spell != null) {
                    var handler = SpellUtil.getSpellHandler(livingEntity);
                    DynamicDimensionFactory.spawnInArena(entity, arena, this.spell, !this.hasEnteredPortal);
                    if (handler.isArenaOwner(this.arenaId)) {
                        handler.setLastArenaEntered(this.arenaId);
                        handler.setLastArenaPosition(this.frontTopLeft);
                    }

                    if (!this.hasEnteredPortal) {
                        if (this.frontTopLeft != null) {
                            BlockPos blockPos = this.frontTopLeft.offset(-3, 0, -3);
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    BlockPos blockPos1 = blockPos.offset(i, 0, j);
                                    BlockEntity blockEntity1 = level.getBlockEntity(blockPos1);
                                    if (blockEntity1 instanceof SummonBlockEntity summonBlockEntity)
                                        summonBlockEntity.hasEnteredPortal = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
        this.setChanged();
    }

    public ResourceLocation getSpell() {
        return this.spell;
    }

    public void setSpell(ResourceLocation spell) {
        this.spell = spell;
        this.setChanged();
    }

    public int getArenaID() {
        return this.arenaId;
    }

    public void setArenaID(int arenaId) {
        this.arenaId = arenaId;
        this.setChanged();
    }

    public BlockPos getFrontTopLeft() {
        return this.frontTopLeft;
    }

    public void setFrontTopLeft(BlockPos frontTopLeft) {
        this.frontTopLeft = frontTopLeft;
    }

    public boolean shouldRenderFace(Direction face) {
        return face.getAxis() == Direction.Axis.Y;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null)
            tag.putUUID("Owner", this.owner);

        if (this.spell != null)
            tag.putString("Spell", this.spell.toString());

        tag.putInt("ArenaId", this.arenaId);
        if (this.frontTopLeft != null)
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, this.frontTopLeft).resultOrPartial(LOGGER::error).ifPresent(nbt -> tag.put("PortalPos", nbt));

        tag.putBoolean("EnteredPortal", this.hasEnteredPortal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        String uuid = tag.getString("Owner");
        if (!uuid.isEmpty())
            this.owner = UUID.fromString(uuid);

        this.spell = ResourceLocation.tryParse(tag.getString("Spell"));
        this.arenaId = tag.getInt("ArenaId");
        BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get("PortalPos")).resultOrPartial(LOGGER::error).ifPresent(blockPos -> this.frontTopLeft = blockPos);
        this.hasEnteredPortal = tag.getBoolean("EnteredPortal");
    }
}
