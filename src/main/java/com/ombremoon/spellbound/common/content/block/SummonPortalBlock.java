package com.ombremoon.spellbound.common.content.block;

import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.world.dimension.DynamicDimensionFactory;
import com.ombremoon.spellbound.common.content.world.dimension.TestDimensionFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SummonPortalBlock extends BaseEntityBlock {
    public static final MapCodec<SummonPortalBlock> CODEC = simpleCodec(SummonPortalBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0, 6.0, 0.0, 16.0, 12.0, 16.0);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public SummonPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.level().isClientSide || entity.getServer() == null)
            return;

        if (entity.isOnPortalCooldown())
            return;

        MinecraftServer server = entity.getServer();
        boolean canTeleportInDimension = entity.level().dimension() == Level.NETHER || entity.level().dimension() == Level.OVERWORLD;
        if (canTeleportInDimension && entity.canUsePortal(false)
                && Shapes.joinIsNotEmpty(
                        Shapes.create(entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ())),
                state.getShape(level, pos),
                BooleanOp.AND
        )) {
            var levelKey = ResourceKey.create(Registries.DIMENSION, CommonClass.customLocation("test"));
            ServerLevel arena = DynamicDimensionFactory.getOrCreateWorld(server, levelKey, TestDimensionFactory::createDimension);
            if (arena != null)
                DynamicDimensionFactory.spawnInArena(entity, arena);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double d0 = (double)pos.getX() + random.nextDouble();
        double d1 = (double)pos.getY() + 0.8;
        double d2 = (double)pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }
}
