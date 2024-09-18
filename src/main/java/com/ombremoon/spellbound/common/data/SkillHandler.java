package com.ombremoon.spellbound.common.data;

import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.networking.PayloadHandler;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SkillHandler implements INBTSerializable<CompoundTag> {
    protected final Map<SpellPath, Float> pathXp = new Object2FloatOpenHashMap<>();
    public final Map<SpellType<?>, Float> spellXp = new Object2FloatOpenHashMap<>();
    protected final Map<SpellType<?>, Set<Skill>> unlockedSkills = new Object2ObjectOpenHashMap<>();

    public void sync(Player player) {
        PayloadHandler.syncSkillsToClient(player);
    }

    public void resetSpellXP(SpellType<?> spellType) {
        spellXp.put(spellType, 0f);
    }

    public int getPathLevel(SpellPath path) {
        if (pathXp.get(path) == null) return 0;
        return (int) Math.floor((double) pathXp.get(path) / 100D); //TODO: Sort xp per level
    }

    public float getPathXp(SpellPath path) {
        if (pathXp.get(path) == null) return 0;
        return pathXp.get(path);
    }

    public float getSpellLevel(SpellType<?> spellType) {
        return (int) Math.floor((double) getSpellXp(spellType) / 10); //TODO: What Duck said
    }

    public float getSpellXp(SpellType<?> spellType) {
        spellXp.putIfAbsent(spellType, 0F);
        return spellXp.get(spellType);
    }

    public void awardSpellXp(SpellType<?> spellType, float xp) {
        spellXp.put(spellType, getSpellXp(spellType) + xp);
        pathXp.put(spellType.getPath(), getPathXp(spellType.getPath()) + xp);
    }

    public <T extends AbstractSpell> void resetSkills(Supplier<SpellType<T>> spellType) {
        this.unlockedSkills.put(spellType.get(), new HashSet<>());
    }

    public <T extends AbstractSpell> void unlockSkill(Supplier<SpellType<T>> spellType, Skill skill) {
        Set<Skill> unlocked = unlockedSkills.get(spellType.get());
        if (unlocked == null) unlocked = new HashSet<>();
        unlocked.add(skill);
        unlockedSkills.put(spellType.get(), unlocked);
    }

    public boolean canUnlockSkill(SpellType<?> spellType, Supplier<Skill> skill) {
        if ((float) skill.get().getXpCost() > getSpellXp(spellType)) return false;
        if (hasSkill(spellType, skill)) return false;

        Set<Skill> unlocked = unlockedSkills.get(spellType);
        for (Supplier<Skill> prereq : skill.get().getPrereqs()) {
            if (unlocked.contains(prereq.get())) return true;
        }

        return false;
    }

    public boolean hasSkill(SpellType<?> spellType, Supplier<Skill> skill) {
        if (unlockedSkills.get(spellType) == null) return false;
        return unlockedSkills.get(spellType).contains(skill.get());
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
        ListTag pathTag = compoundTag.getList("PathXp", 10);
        ListTag spellTag = compoundTag.getList("SpellXp", 10);
        ListTag skillTag = compoundTag.getList("Skills", 10);

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
            Set<Skill> skills = new HashSet<>();
            ListTag skillList = tag.getList("Skills", ListTag.TAG_LIST);
            for (int j = 0; j < skillList.size(); j++) {
                skills.add(SkillInit.REGISTRY.get(ResourceLocation.tryParse(skillList.getString(j))));
            }
            this.unlockedSkills.put(SpellInit.REGISTRY.get(ResourceLocation.tryParse(tag.getString("Spell"))), skills);
        }

    }
}
