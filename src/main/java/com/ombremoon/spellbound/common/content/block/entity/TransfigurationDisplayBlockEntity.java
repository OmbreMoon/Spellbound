package com.ombremoon.spellbound.common.content.block.entity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.ombremoon.sentinellib.api.Easing;
import com.ombremoon.spellbound.common.content.world.multiblock.TransfigurationMultiblockPart;
import com.ombremoon.spellbound.common.init.SBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TransfigurationDisplayBlockEntity extends TransfigurationMultiblockPart {
    public ItemStack currentItem;
    public float rot;
    public float oRot;
    public float tRot;
    public int time;
    public boolean active;
    public int spiralTime;
    public double dx, dy, dz, r0, theta0;
    public long t0;

    public TransfigurationDisplayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public TransfigurationDisplayBlockEntity(BlockPos pos, BlockState blockState) {
        super(SBBlockEntities.TRANSFIGURATION_DISPLAY.get(), pos, blockState);
    }

    public static void itemAnimationTick(Level level, BlockPos pos, BlockState state, TransfigurationDisplayBlockEntity display) {
        if (!display.active) {
            display.oRot = display.rot;
            display.tRot += 0.02F;

            while (display.rot < (float) -Math.PI) {
                display.rot += (float) (Math.PI * 2);
            }

            while (display.tRot >= (float) Math.PI) {
                display.tRot -= (float) (Math.PI * 2);
            }

            while (display.tRot < (float) -Math.PI) {
                display.tRot += (float) (Math.PI * 2);
            }

            float f2 = display.tRot - display.rot;

            while (f2 >= (float) Math.PI) {
                f2 -= (float) (Math.PI * 2);
            }

            while (f2 < (float) -Math.PI) {
                f2 += (float) (Math.PI * 2);
            }

            display.rot += f2 * 0.4F;
            display.time++;
        } else {
            tickItems(display);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TransfigurationDisplayBlockEntity display) {
        if (display.active)
            tickItems(display);
    }

    private static void tickItems(TransfigurationDisplayBlockEntity display) {
        display.spiralTime++;
        if (display.spiralTime >= display.getRitual().definition().startupTime()) {
            resetDisplay(display);
        }
    }

    private static void resetDisplay(TransfigurationDisplayBlockEntity display) {
        display.setItem(null);
        display.dx = 0;
        display.dy = 0;
        display.dz = 0;
        display.r0 = 0;
        display.theta0 = 0;
        display.t0 = 0;
        display.spiralTime = 0;
        display.active = false;
        display.setRitual(null);
        display.setChanged();
    }

    public void initSpiral(BlockPos center) {
        this.dx = worldPosition.getX() - center.getX();
        this.dy = worldPosition.getY() - center.getY();
        this.dz = worldPosition.getZ() - center.getZ();
        this.r0 = Math.hypot(dx, dz);
        this.theta0 = Math.atan2(dz, dx);
        if (theta0 < 0) theta0 += Math.PI * 2;
        this.t0 = level.getGameTime();
    }

    public Vec3 spiralOffset(float partialTicks, double turns) {
        double raw = ((level.getGameTime() - t0) + partialTicks) / this.getRitual().definition().startupTime();
        double p = Mth.clamp(raw, 0.0, 1.0);
        double ease = 1.0 - Math.pow(1.0 - p, 3.0);

        double radius = r0 * (1.0 - ease);
        double theta = theta0 + turns * Math.PI * 2 * ease;

        double x = radius * Math.cos(theta);
        double z = radius * Math.sin(theta);
        return new Vec3(x - dx, -dy, z - dz);
    }

    public void setItem(ItemStack stack) {
        this.currentItem = stack;
        setChanged();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag compoundTag = new CompoundTag();
        if (this.currentItem != null) {
            ItemStack.CODEC
                    .encodeStart(NbtOps.INSTANCE, this.currentItem)
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(nbt -> compoundTag.put("CurrentItem", nbt));
        }
        return compoundTag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.currentItem != null) {
            ItemStack.CODEC
                    .encodeStart(NbtOps.INSTANCE, this.currentItem)
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(nbt -> tag.put("CurrentItem", nbt));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CurrentItem", 10)) {
            DataResult<ItemStack> dataResult = ItemStack.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, tag.get("CurrentItem")));
            dataResult.resultOrPartial(LOGGER::error).ifPresent(this::setItem);
        }
    }
}
