package com.ombremoon.spellbound.common.data;

import com.ombremoon.spellbound.common.content.skill.Skill;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SkillHandler implements INBTSerializable<CompoundTag> {
    protected final Map<SpellPath, Float> pathXp = new HashMap<>();
    protected final Map<SpellType<?>, Float> spellXp = new HashMap<>();
    protected final Map<SpellType<?>, Set<Skill>> unlockedSkills = new HashMap<>();

    public int getPathLevel(SpellPath path) {
        return (int) Math.floor((double) pathXp.get(path) / 100D);
    }

    public float getPathXp(SpellPath path) {
        return pathXp.get(path);
    }

    public float getSpellXp(Supplier<SpellType<?>> spellType) {
        return spellXp.get(spellType.get());
    }

    public void awardSpellXp(Supplier<SpellType<?>> spellType, float xp) {
        spellXp.put(spellType.get(), getSpellXp(spellType) + xp);
        pathXp.put(spellType.get().path, xp);
    }

    public void unlockSkill(Supplier<SpellType<?>> spellType, Skill skill) {
        Set<Skill> unlocked = unlockedSkills.get(spellType.get());
        unlocked.add(skill);
        unlockedSkills.put(spellType.get(), unlocked);
    }

    public boolean canUnlockSkill(Supplier<SpellType<?>> spellType, Skill skill) {
        if ((float) skill.xpCost > getSpellXp(spellType)) return false;
        if (hasSkill(spellType, skill)) return false;

        Set<Skill> unlocked = unlockedSkills.get(spellType.get());
        for (Skill prereq : skill.prerequisites) {
            if (!unlocked.contains(prereq)) return false;
        }

        return true;
    }

    public boolean hasSkill(Supplier<SpellType<?>> spellType, Skill skill) {
        return unlockedSkills.get(spellType.get()).contains(skill);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {

    }
}
