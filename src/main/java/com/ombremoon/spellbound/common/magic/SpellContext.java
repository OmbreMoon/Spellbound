package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SpellContext {
    private final SpellType<?> spellType;
    private final LivingEntity caster;
    private final Level level;
    private final BlockPos blockPos;
    private final ItemStack itemStack;
    @Nullable
    private final Entity target;
    private final SpellHandler spellHandler;
    private final SkillHolder skillHolder;
    private final boolean isRecast;

    public SpellContext(SpellType<?> spellType, LivingEntity caster, boolean isRecast) {
        this(spellType, caster, caster.level(), caster.getOnPos(), caster.getOffhandItem(), null, isRecast);
    }

    public SpellContext(SpellType<?> spellType, LivingEntity caster, Entity target, boolean isRecast) {
        this(spellType, caster, caster.level(), caster.getOnPos(), caster.getOffhandItem(), target, isRecast);
    }

    public SpellContext(SpellType<?> spellType, LivingEntity caster, Level level, BlockPos blockPos, Entity target, boolean isRecast) {
        this(spellType, caster, level, blockPos, caster.getOffhandItem(), target, isRecast);
    }

    public SpellContext(SpellType<?> spellType, LivingEntity caster, Level level, BlockPos blockPos, ItemStack itemStack, Entity target, boolean isRecast) {
        this.spellType = spellType;
        this.caster = caster;
        this.level = level;
        this.blockPos = blockPos;
        this.itemStack = itemStack;
        this.target = target;
        this.spellHandler = SpellUtil.getSpellHandler(caster);
        this.skillHolder = SpellUtil.getSkillHolder(caster);
        this.isRecast = isRecast;
    }

    public SpellHandler getSpellHandler() {
        return this.spellHandler;
    }

    public SkillHolder getSkills() {
        return skillHolder;
    }

    public boolean isRecast() {
        return this.isRecast;
    }

    public LivingEntity getCaster() {
        return this.caster;
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

    public @Nullable Entity getTarget() {
        return this.target;
    }

    public float getRotation() {
        return this.caster.getYRot();
    }

    public boolean hasCatalyst(Item catalyst) {
        return this.caster.isHolding(catalyst);
    }

    public boolean hasActiveSpells(int amount) {
        return this.spellHandler.getActiveSpells(this.spellType).size() >= amount;
    }

    public int getFlag() {
        return this.spellHandler.getFlag(this.spellType);
    }
}
