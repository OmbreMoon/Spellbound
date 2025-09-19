package com.ombremoon.spellbound.common.content.world.multiblock.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.content.block.entity.TransfigurationDisplayBlockEntity;
import com.ombremoon.spellbound.common.content.world.multiblock.BuildingBlock;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockIndex;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockOutput;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockSerializer;
import com.ombremoon.spellbound.common.init.SBBlocks;
import com.ombremoon.spellbound.common.init.SBMultiblockSerializers;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.RitualHelper;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.RitualInstance;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.RitualSavedData;
import com.ombremoon.spellbound.common.magic.acquisition.transfiguration.TransfigurationRitual;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO: FIX MULTI CLICK RITUAL

public class TransfigurationMultiblock extends StandardMultiblock {
    public static final List<Block> EXCLUDED_BLOCKS = List.of(
            SBBlocks.RUNE.get(),
            SBBlocks.TRANSFIGURATION_PEDESTAL.get(),
            SBBlocks.TRANSFIGURATION_DISPLAY.get(),
            Blocks.WATER,
            Blocks.LAVA
    );
    protected final int rings;
    private final List<MultiblockIndex> displayPositions = new ObjectArrayList<>();

    public TransfigurationMultiblock(MultiblockStructure structure, int rings) {
        super(structure);
        this.rings = rings;
        this.initializeDisplays();
    }

    @Override
    public MultiblockSerializer<?> getSerializer() {
        return SBMultiblockSerializers.TRANSFIGURATION_MULTIBLOCK.get();
    }

    public int getRings() {
        return this.rings;
    }

    public List<MultiblockIndex> getDisplayPositions() {
        return this.displayPositions;
    }

    public BlockPos getPedestalPosition(MultiblockPattern pattern) {
        return this.getActiveIndex().toPos(pattern.facing(), pattern.frontBottomLeft());
    }

    @Override
    public void onActivate(Player player, Level level, MultiblockPattern pattern) {
        BlockPos origin = pattern.frontBottomLeft();
        BlockPos pedestal = this.getPedestalPosition(pattern);
        List<TransfigurationDisplayBlockEntity> displays = this.displayPositions
                .stream()
                .map(index -> index.toPos(pattern.facing(), origin))
                .map(level::getBlockEntity).filter(Objects::nonNull)
                .map(blockEntity -> (TransfigurationDisplayBlockEntity) blockEntity)
                .toList();
        List<ItemStack> items = displays
                .stream()
                .map(display -> display.currentItem)
                .toList();
        Optional<TransfigurationRitual> optional = RitualHelper.getRitualFor(level, this, items);
        if (optional.isPresent()) {
            TransfigurationRitual ritual = optional.get();
            displays.forEach(display -> {
                if (!display.active) {
                    display.setRitual(ritual);
                    display.setCenter(pedestal);
                    display.active = true;
                    level.sendBlockUpdated(display.getBlockPos(), display.getBlockState(), display.getBlockState(), 3);
                }
            });

            if (!level.isClientSide)
                RitualSavedData.addRitual(level, new RitualInstance(Holder.direct(ritual), player.getUUID(), pedestal, pattern));
        } else {
            clearMultiblock(player, level, pattern);
        }
    }

    @Override
    public void debugMultiblock(Level level, BlockPos blockPos, Direction facing) {
        BlockPos originFromPedestal = this.locateOrigin(this.getActiveIndex(), blockPos, facing);
        super.debugMultiblock(level, originFromPedestal, facing);
    }

    private void initializeDisplays() {
        for (var entry : this.indices.entrySet()) {
            MultiblockIndex index = entry.getKey();
            BuildingBlock block = entry.getValue();
            if (block.test(SBBlocks.TRANSFIGURATION_DISPLAY.get().defaultBlockState()))
                this.displayPositions.add(index);
        }
    }

    public static class Builder extends StandardMultiblock.Builder {
        protected int rings;

        public Builder() {
            this.key.put('*', BuildingBlock.ANY);
            this.key.put(' ', BuildingBlock.EMPTY);
            this.key.put('^', BuildingBlock.of(SBBlocks.RUNE.get()));
            this.key.put('$', BuildingBlock.of(SBBlocks.TRANSFIGURATION_DISPLAY.get()));
            this.key.put('#', BuildingBlock.of(SBBlocks.TRANSFIGURATION_PEDESTAL.get()));
        }

        public static Builder of() {
            return new Builder();
        }

        public Builder rings(int rings) {
            if (rings == 1) {
                this.pattern("**^^^**",
                             "*$   $*",
                             "^     ^",
                             "^  #  ^",
                             "^     ^",
                             "*$   $*",
                             "**^^^**");
                this.index(3, 0, 3);
                this.rings = 1;
            } else if (rings == 2) {
                this.pattern("***********",
                             "***********",
                             "****^^^****",
                             "***$   $***",
                             "**^     ^**",
                             "**^  #  ^**",
                             "**^     ^**",
                             "***$   $***",
                             "****^^^****",
                             "***********",
                             "***********");
                this.pattern("***^^^^^***",
                             "**$     $**",
                             "*$       $*",
                             "^         ^",
                             "^         ^",
                             "^         ^",
                             "^         ^",
                             "^         ^",
                             "*$       $*",
                             "**$     $**",
                             "***^^^^^***");
                this.rings = 2;
                this.index(5, 0, 5);
            } else if (rings == 3) {
                this.pattern("  ^^^  ",
                             " $   $ ",
                             "^     ^",
                             "^  #  ^",
                             "^     ^",
                             " $   $ ",
                             "  ^^^  ");
                this.pattern("   ^^^^^   ",
                             "  $     $  ",
                             " $       $ ",
                             "^         ^",
                             "^         ^",
                             "^    #    ^",
                             "^         ^",
                             "^         ^",
                             " $       $ ",
                             "  $     $  ",
                             "   ^^^^^   ");
                this.pattern("    ^^^^^^^    ",
                             "   $       $   ",
                             "  $         $  ",
                             " $           $ ",
                             "^             ^",
                             "^             ^",
                             "^             ^",
                             "^      #      ^",
                             "^             ^",
                             "^             ^",
                             "^             ^",
                             " $           $ ",
                             "  $         $  ",
                             "   $       $   ",
                             "    ^^^^^^^    ");
                this.rings = 3;
                this.index(7, 0, 7);
            } else {
                throw new IllegalArgumentException("Ring builder does not only supports rings of length 1 to 3.");
            }

            return this;
        }

        @Override
        public void build(MultiblockOutput output, ResourceLocation location) {
            MultiblockStructure structure = MultiblockStructure.of(this.key, this.pattern, this.activeIndex);
            TransfigurationMultiblock multiblock = new TransfigurationMultiblock(structure, this.rings);
            output.accept(location, multiblock);
        }
    }

    public static class Serializer implements MultiblockSerializer<TransfigurationMultiblock> {
        public static final MapCodec<TransfigurationMultiblock> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        MultiblockStructure.MAP_CODEC.forGetter(transfigurationMultiblock -> transfigurationMultiblock.structure),
                        Codec.INT.fieldOf("rings").forGetter(transfigurationMultiblock -> transfigurationMultiblock.rings)
                ).apply(instance, TransfigurationMultiblock::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, TransfigurationMultiblock> STREAM_CODEC = StreamCodec.of(
                TransfigurationMultiblock.Serializer::toNetwork, TransfigurationMultiblock.Serializer::fromNetwork
        );

        @Override
        public MapCodec<TransfigurationMultiblock> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TransfigurationMultiblock> streamCodec() {
            return STREAM_CODEC;
        }

        private static TransfigurationMultiblock fromNetwork(RegistryFriendlyByteBuf buffer) {
            MultiblockStructure structure = MultiblockStructure.STREAM_CODEC.decode(buffer);
            int rings = ByteBufCodecs.INT.decode(buffer);
            return new TransfigurationMultiblock(structure, rings);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, TransfigurationMultiblock multiblock) {
            MultiblockStructure.STREAM_CODEC.encode(buffer, multiblock.structure);
            ByteBufCodecs.INT.encode(buffer, multiblock.rings);
        }
    }
}
