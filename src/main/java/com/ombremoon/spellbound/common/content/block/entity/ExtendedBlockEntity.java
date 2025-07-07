package com.ombremoon.spellbound.common.content.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ExtendedBlockEntity extends BlockEntity {
    public BlockPos center;
    public boolean isPlaced; //True once the whole placing logic runs (to prevent updateShape from breaking it early)

    public ExtendedBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.center = this.getBlockPos();
        this.isPlaced = false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("center", NbtUtils.writeBlockPos(this.center));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.center = NbtUtils.readBlockPos(tag,"center").get();
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public void setCenter(BlockPos pos) {
        center = pos;
    }

    public void setPlaced(){
        isPlaced = true;
    }

    public static void setPlaced(LevelReader level, BlockPos blockPos) {
        if(level.getBlockEntity(blockPos) instanceof ExtendedBlockEntity entity) entity.setPlaced();
    }
}
