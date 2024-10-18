package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SpellContext {
    private final LivingEntity caster;
    private final Level level;
    private final BlockPos blockPos;
    private final ItemStack itemStack;
    @Nullable
    private final LivingEntity target;
    private final SpellHandler spellHandler;
    private final SkillHolder skillHolder;
    private final boolean isRecast;

    public SpellContext(LivingEntity caster, boolean isRecast) {
        this(caster, caster.level(), caster.getOnPos(), caster.getOffhandItem(), null, isRecast);
    }

    public SpellContext(LivingEntity caster, LivingEntity target, boolean isRecast) {
        this(caster, caster.level(), caster.getOnPos(), caster.getOffhandItem(), target, isRecast);
    }

    public SpellContext(LivingEntity caster, Level level, BlockPos blockPos, LivingEntity target, boolean isRecast) {
        this(caster, level, blockPos, caster.getOffhandItem(), target, isRecast);
    }

    public SpellContext(LivingEntity caster, Level level, BlockPos blockPos, ItemStack itemStack, LivingEntity target, boolean isRecast) {
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

    public @Nullable LivingEntity getTarget() {
        return this.target;
    }

    public float getRotation() {
        return this.caster.getYRot();
    }

    public boolean hasActiveSpells(SpellType<?> spell, int amount) {
        return this.spellHandler.getActiveSpells(spell).size() >= amount;
    }
}
