package com.ombremoon.spellbound.common.magic.acquisition.bosses;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.init.SBBossFights;
import com.ombremoon.spellbound.main.Keys;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public class EntityBasedBossFight extends BossFight {
    public static final Codec<EntityBasedBossFight> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().fieldOf("bosses").forGetter(bossFight -> bossFight.bosses),
                    BlockPos.CODEC.fieldOf("blockScanStart").forGetter(bossFight -> bossFight.blockScanStart),
                    BlockPos.CODEC.fieldOf("blockScanEnd").forGetter(bossFight -> bossFight.blockScanEnd),
                    Vec3.CODEC.fieldOf("playerSpawnOffset").forGetter(bossFight -> bossFight.playerSpawnOffset),
                    DimensionData.CODEC.fieldOf("dimensionData").forGetter(bossFight -> bossFight.dimensionData)
            ).apply(instance, EntityBasedBossFight::new)
    );

    private final List<EntityType<?>> bosses;

    public EntityBasedBossFight(List<EntityType<?>> bosses, BlockPos blockScanStart, BlockPos blockScanEnd, Vec3 playerSpawnOffset, DimensionData dimensionData) {
        super(blockScanStart, blockScanEnd, playerSpawnOffset, dimensionData);
        this.bosses = bosses;
    }

    @Override
    public BossFightInstance<?, ?> createFight(ServerLevel level) {
        return new Instance(this);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Instance extends BossFightInstance<EntityBasedBossFight, Instance> {
        public static final MapCodec<Instance> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        EntityBasedBossFight.CODEC.fieldOf("bossFight").forGetter(inst -> inst.bossFight)
                ).apply(instance, Instance::new)
        );
        final List<Integer> bosses = new ObjectArrayList<>();

        Instance(EntityBasedBossFight bossFight) {
            super(bossFight);
        }

        @Override
        public void initializeWinCondition(ServerLevel level, EntityBasedBossFight bossFight) {
            AABB bossScanArea = AABB.encapsulatingFullBlocks(bossFight.blockScanStart, bossFight.blockScanEnd);
            List<LivingEntity> bosses = level.getEntitiesOfClass(LivingEntity.class, bossScanArea);
            for (LivingEntity boss : bosses) {
                if (bossFight.bosses.contains(boss.getType()))
                    this.bosses.add(boss.getId());
            }
        }

        @Override
        public void tickFight(ServerLevel level, EntityBasedBossFight bossFight) {
            this.bosses.removeIf(id -> {
                Entity entity = level.getEntity(id);
                return entity == null || !entity.isAlive();
            });

            super.tickFight(level, bossFight);

        }

        @Override
        public boolean winCondition(ServerLevel level, EntityBasedBossFight bossFight) {
            return this.bosses.isEmpty();
        }

        @Override
        public void endFight(ServerLevel level, EntityBasedBossFight bossFight) {
            for (int id : this.bosses) {
                Entity entity = level.getEntity(id);
                if (entity != null)
                    entity.discard();
            }
        }

        @Override
        public MapCodec<Instance> codec() {
            return SBBossFights.DEFAULT.get();
        }

        @Override
        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
            nbt.putIntArray("Bosses", this.bosses);
            return nbt;
        }

        @Override
        public void load(CompoundTag nbt) {
            if (nbt.contains("Bosses", 11)) {
                for (int i : nbt.getIntArray("Bosses")) {
                    this.bosses.add(i);
                }
            }
        }
    }

    public static class Builder implements BossFightBuilder<EntityBasedBossFight> {
        private final List<Supplier<? extends EntityType<?>>> bosses = new ObjectArrayList<>();
        private BlockPos blockScanStart = BlockPos.ZERO;
        private BlockPos blockScanEnd = BlockPos.ZERO;
        private Vec3 playerSpawnOffset = Vec3.ZERO;

        public Builder withBoss(Supplier<? extends EntityType<?>> entity) {
            this.bosses.add(entity);
            return this;
        }

        public Builder scanFrom(int x, int y, int z) {
            this.blockScanStart = new BlockPos(x, y, z);
            return this;
        }

        public Builder scanTo(int x, int y, int z) {
            this.blockScanEnd = new BlockPos(x, y, z);
            return this;
        }

        public Builder spawnAt(int x, int y, int z) {
            this.playerSpawnOffset = new Vec3(x, y, z);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntityBasedBossFight build() {
            List<EntityType<?>> bosses = (List<EntityType<?>>) this.bosses.stream().map(Supplier::get).toList();
            return new EntityBasedBossFight(
                    bosses,
                    this.blockScanStart,
                    this.blockScanEnd,
                    this.playerSpawnOffset,
                    new DimensionData(Keys.EMPTY_BIOME, DimensionData.Weather.CLEAR));
        }
    }
}
