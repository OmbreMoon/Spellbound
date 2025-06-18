package com.ombremoon.spellbound.common.content.world.multiblock;

import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.TransfigurationRitual;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public abstract class TransfigurationMultiblockPart extends BlockEntity implements MultiblockPart {
    protected static final Logger LOGGER = Constants.LOG;
    private Multiblock multiblock;
    private MultiblockIndex index;
    private Direction facing;
    private TransfigurationRitual ritual;

    public TransfigurationMultiblockPart(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public Multiblock getMultiblock() {
        return this.multiblock;
    }

    @Override
    public MultiblockIndex getIndex() {
        return this.index;
    }

    @Override
    public void setIndex(Multiblock multiblock, MultiblockIndex index, Direction facing) {
        this.multiblock = multiblock;
        this.index = index;
        this.facing = facing;
    }

    @Override
    public Direction getPartDirection() {
        return this.facing;
    }

    @Override
    public boolean isAssigned() {
        return this.multiblock != null;
    }

    public TransfigurationRitual getRitual() {
        return this.ritual;
    }

    public void setRitual(TransfigurationRitual ritual) {
        this.ritual = ritual;
    }
}
