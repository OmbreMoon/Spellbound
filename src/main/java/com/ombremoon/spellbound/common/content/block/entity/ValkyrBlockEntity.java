package com.ombremoon.spellbound.common.content.block.entity;

import com.ombremoon.spellbound.common.content.block.ValkyrStatueBlock;
import com.ombremoon.spellbound.common.init.SBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ValkyrBlockEntity extends SimpleExtendedBlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ValkyrBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public ValkyrBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(SBBlockEntities.VALKY_STATUE.get(), pPos, pBlockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::valkyrStatueController));
    }

    protected <T extends GeoAnimatable> PlayState valkyrStatueController(AnimationState<T> data) {
        int pose = this.getBlockState().getValue(ValkyrStatueBlock.POSE);
        switch (pose) {
            case 1 -> data.setAnimation(RawAnimation.begin().thenLoop("walk"));
            case 2 -> data.setAnimation(RawAnimation.begin().thenLoop("idle_fly"));
            case 3 -> data.setAnimation(RawAnimation.begin().thenLoop("run"));
            default -> data.setAnimation(RawAnimation.begin().thenLoop("idle"));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
