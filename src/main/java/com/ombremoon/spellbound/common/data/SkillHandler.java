package com.ombremoon.spellbound.common.data;

import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SkillHandler implements INBTSerializable<CompoundTag> {
    protected final Map<SpellPath, Float> pathXp = new HashMap<>();
    protected final Map<SpellType<?>, Float> spellXp = new HashMap<>();
    protected final Map<SpellType<?>, Set<Skill>> unlockedSkills = new HashMap<>();

    public int getPathLevel(SpellPath path) {
        if (pathXp.get(path) == null) return 0;
        return (int) Math.floor((double) pathXp.get(path) / 100D);
    }

    public float getPathXp(SpellPath path) {
        if (pathXp.get(path) == null) return 0;
        return pathXp.get(path);
    }

    public float getSpellXp(Supplier<? extends SpellType<?>> spellType) {
        if (spellXp.get(spellType.get()) == null) return 0;
        return spellXp.get(spellType.get());
    }

    public void awardSpellXp(Supplier<? extends SpellType<?>> spellType, float xp) {
        spellXp.put(spellType.get(), getSpellXp(spellType) + xp);
        pathXp.put(spellType.get().path, getPathXp(spellType.get().path) + xp);
    }

    public void unlockSkill(Supplier<SpellType<?>> spellType, Skill skill) {
        Set<Skill> unlocked = unlockedSkills.get(spellType.get());
        unlocked.add(skill);
        unlockedSkills.put(spellType.get(), unlocked);
    }

    public boolean canUnlockSkill(Supplier<SpellType<?>> spellType, Skill skill) {
        if ((float) skill.getXpCost() > getSpellXp(spellType)) return false;
        if (hasSkill(spellType, skill)) return false;

        Set<Skill> unlocked = unlockedSkills.get(spellType.get());
        for (Skill prereq : skill.getPrereqs()) {
            if (unlocked.contains(prereq)) return true;
        }

        return false;
    }

    public boolean hasSkill(Supplier<SpellType<?>> spellType, Skill skill) {
        if (unlockedSkills.get(spellType.get()) == null) return false;
        return unlockedSkills.get(spellType.get()).contains(skill);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag pathxpTag = new ListTag();
        ListTag spellXpTag = new ListTag();
        ListTag skillsTag = new ListTag();

        for (SpellType<?> spellType : this.spellXp.keySet()) {
            CompoundTag newTag = new CompoundTag();
            newTag.putString("Spell", spellType.getResourceLocation().toString());
            newTag.putFloat("Xp", this.spellXp.get(spellType));
            spellXpTag.add(newTag);
        }

        for (SpellType<?> spellType : unlockedSkills.keySet()) {
            CompoundTag newTag = new CompoundTag();
            newTag.putString("Spell", spellType.getResourceLocation().toString());
            ListTag savedSkills = new ListTag();
            for (Skill skill : unlockedSkills.get(spellType)) {
                CompoundTag newSkillTag = new CompoundTag();
                newSkillTag.putString("Skill", skill.getResourceLocation().toString());
                savedSkills.add(newSkillTag);
            }
            newTag.put("Skills", savedSkills);
            skillsTag.add(newTag);
        }

        for (SpellPath path : this.pathXp.keySet()) {
            CompoundTag newTag = new CompoundTag();
            newTag.putString("Path", path.toString());
            newTag.putFloat("Xp", this.pathXp.get(path));
            pathxpTag.add(newTag);
        }

        tag.put("PathXp", pathxpTag);
        tag.put("SpellXp", spellXpTag);
        tag.put("Skills", skillsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        ListTag pathTag = compoundTag.getList("PathXp", ListTag.TAG_LIST);
        ListTag spellTag = compoundTag.getList("SpellXp", ListTag.TAG_LIST);
        ListTag skillTag = compoundTag.getList("Skills", ListTag.TAG_LIST);

        for (int i = 0; i < pathTag.size(); i++) {
            CompoundTag tag = pathTag.getCompound(i);
            this.pathXp.put(SpellPath.valueOf(tag.getString("Path")), tag.getFloat("Xp"));
        }

        for (int i = 0; i < spellTag.size(); i++) {
            CompoundTag tag = spellTag.getCompound(i);
            this.spellXp.put(SpellInit.REGISTRY.get(ResourceLocation.tryParse(tag.getString("Spell"))), tag.getFloat("Xp"));
        }

        for (int i = 0; i < skillTag.size(); i++) {
            CompoundTag tag = skillTag.getCompound(i);
            Set<Skill> skills = Set.of();
            ListTag skillList = tag.getList("Skills", ListTag.TAG_LIST);
            for (int j = 0; j < skillList.size(); j++) {
                skills.add(SkillInit.REGISTRY.get(ResourceLocation.tryParse(skillList.getString(j))));
            }
            this.unlockedSkills.put(SpellInit.REGISTRY.get(ResourceLocation.tryParse(tag.getString("Spell"))), skills);
        }

    }
}
