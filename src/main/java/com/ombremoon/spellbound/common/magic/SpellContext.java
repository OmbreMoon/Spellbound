package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SpellContext {
    private final Player player;
    private final Level level;
    private final BlockPos blockPos;
    private final ItemStack itemStack;
    @Nullable
    private final LivingEntity target;
    private final SpellHandler spellHandler;
    private final SkillHandler skillHandler;
    private final boolean isRecast;

    public SpellContext(Player player, LivingEntity target, boolean isRecast) {
        this(player, player.level(), player.getOnPos(), player.getOffhandItem(), target, isRecast);
    }

    public SpellContext(Player player, Level level, BlockPos blockPos, LivingEntity target, boolean isRecast) {
        this(player, level, blockPos, player.getOffhandItem(), target, isRecast);
    }

    public SpellContext(Player player, Level level, BlockPos blockPos, ItemStack itemStack, LivingEntity target, boolean isRecast) {
        this.player = player;
        this.level = level;
        this.blockPos = blockPos;
        this.itemStack = itemStack;
        this.target = target;
        this.spellHandler = SpellUtil.getSpellHandler(player);
        this.skillHandler = player.getData(DataInit.SKILL_HANDLER);
        this.isRecast = isRecast;
    }

    public SpellHandler getSpellHandler() {
        return this.spellHandler;
    }

    public SkillHandler getSkillHandler() {
        return skillHandler;
    }

    public boolean isRecast() {
        return this.isRecast;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Level getLevel() {
        return this.level;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public ItemStack getItemInOffhand() {
        return this.itemStack;
    }

    public @Nullable LivingEntity getTarget() {
        return this.target;
    }

    public float getRotation() {
        return this.player.getYRot();
    }
}
