package com.ombremoon.spellbound.common.magic.acquisition.divine;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.content.item.SpellTomeItem;
import com.ombremoon.spellbound.common.init.SBBlocks;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ActionRewards(int experience, int judgement, List<ResourceLocation> spells, List<ResourceKey<LootTable>> loot) {
    public static final Codec<ActionRewards> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.optionalFieldOf("experience", Integer.valueOf(0)).forGetter(ActionRewards::experience),
                    Codec.INT.optionalFieldOf("judgement", Integer.valueOf(0)).forGetter(ActionRewards::judgement),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("spells", java.util.List.of()).forGetter(ActionRewards::spells),
                    ResourceKey.codec(Registries.LOOT_TABLE).listOf().optionalFieldOf("loot", List.of()).forGetter(ActionRewards::loot)
            ).apply(instance, ActionRewards::new)
    );
    public static final ActionRewards EMPTY = new ActionRewards(0, 0, List.of(), List.of());

    public void grant(ServerPlayer player) {
        player.giveExperiencePoints(this.experience);
        LootParams lootParams = new LootParams.Builder(player.serverLevel())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .withLuck(player.getLuck())
                .create(LootContextParamSets.ADVANCEMENT_REWARD);
        boolean flag = false;

        BlockState blockState = this.getNearestShrine(player);
        if (blockState != null) {
            for (var key : this.loot) {
                for (ItemStack stack : player.server.reloadableRegistries().getLootTable(key).getRandomItems(lootParams)) {
                    if (this.addOrDropItem(player, stack))
                        flag = true;
                }
            }

            for (ResourceLocation location : this.spells) {
                SpellType<?> spellType = SBSpells.REGISTRY.get(location);
                if (spellType != null) {
                    ItemStack itemStack = SpellTomeItem.createWithSpell(spellType);
                    if (this.addOrDropItem(player, itemStack))
                        flag = true;
                }
            }

            if (flag) {
                player.containerMenu.broadcastChanges();
            }
        }
    }

    private boolean addOrDropItem(Player player, ItemStack stack) {
        if (player.addItem(stack)) {
            player.level()
                    .playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS,
                            0.2F,
                            ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                    );
            return true;
        } else {
            ItemEntity itementity = player.drop(stack, false);
            if (itementity != null) {
                itementity.setNoPickUpDelay();
                itementity.setTarget(player.getUUID());
            }
            return false;
        }
    }

    @Nullable
    private BlockState getNearestShrine(ServerPlayer player) {
        BlockPos pos = player.getOnPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = x - 15; i <= x + 15; i++) {
            for (int j = y - 7; j <= y + 7; j++) {
                for (int k = x - 15; k <= z + 15; k++) {
                    mutableBlockPos.set(i, j, k);
                    BlockState blockState = player.level().getBlockState(mutableBlockPos);
                    if (blockState.is(SBBlocks.DIVINE_SHRINE.get()))
                        return blockState;
                }
            }
        }
        return null;
    }

    public static class Builder {
        private int experience;
        private int judgement;
        private final ImmutableList.Builder<ResourceLocation> spells = ImmutableList.builder();
        private final ImmutableList.Builder<ResourceKey<LootTable>> loot = ImmutableList.builder();

        /**
         * Creates a new builder with the given amount of experience as a reward
         */
        public static Builder experience(int experience) {
            return new Builder().addExperience(experience);
        }

        /**
         * Adds the given amount of experience. (Not a direct setter)
         */
        public Builder addExperience(int experience) {
            this.experience += experience;
            return this;
        }

        public static Builder judgement(int experience) {
            return new Builder().addExperience(experience);
        }

        public Builder addJudgement(int experience) {
            this.experience += experience;
            return this;
        }

        public static Builder spell(SpellType<?> spellType) {
            return new Builder().addSpell(spellType);
        }

        public Builder addSpell(SpellType<?> spellType) {
            this.spells.add(spellType.location());
            return this;
        }

        public static Builder loot(ResourceKey<LootTable> lootTable) {
            return new Builder().addLootTable(lootTable);
        }

        public Builder addLootTable(ResourceKey<LootTable> lootTable) {
            this.loot.add(lootTable);
            return this;
        }

        public ActionRewards build() {
            return new ActionRewards(this.experience, this.judgement, this.spells.build(), this.loot.build());
        }
    }
}
