package com.ombremoon.spellbound.common.data;

import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellModifier;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.ModifierSkill;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.skills.SkillCooldowns;
import com.ombremoon.spellbound.networking.PayloadHandler;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SkillHandler implements INBTSerializable<CompoundTag> {
    protected final Map<SpellPath, Float> pathXp = new Object2FloatOpenHashMap<>();
    protected final Map<SpellType<?>, Float> spellXp = new Object2FloatOpenHashMap<>();
    protected final Map<SpellType<?>, Set<Skill>> unlockedSkills = new Object2ObjectOpenHashMap<>();
    private final Set<SpellModifier> permanentModifiers = new ObjectOpenHashSet<>();
    private final Map<SpellModifier, Integer> timedModifiers = new Object2IntOpenHashMap<>();
    private final SkillCooldowns cooldowns = new SkillCooldowns();

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

    public int getSpellLevel(SpellType<?> spellType) {
        return (int) Math.min(Math.floor((double) getSpellXp(spellType) / 100), 5); //TODO: What Duck said
    }

    public float getSpellXp(SpellType<?> spellType) {
        spellXp.putIfAbsent(spellType, 0F);
        return spellXp.get(spellType);
    }

    public void awardSpellXp(SpellType<?> spellType, float xp) {
        spellXp.put(spellType, Math.min(getSpellXp(spellType) + xp, 500));
        pathXp.put(spellType.getPath(), getPathXp(spellType.getPath()) + (xp / 2));
    }

    public <T extends AbstractSpell> void resetSkills(SpellType<T> spellType) {
        this.unlockedSkills.put(spellType, new HashSet<>());
    }

    public void unlockSkill(Skill skill) {
        Set<Skill> unlocked = unlockedSkills.get(skill.getSpell());
        if (unlocked == null) unlocked = new HashSet<>();
        unlocked.add(skill);
        unlockedSkills.put(skill.getSpell(), unlocked);
        if (skill instanceof ModifierSkill modifierSkill)
            permanentModifiers.addAll(modifierSkill.getModifiers());
    }

    public boolean canUnlockSkill(Skill skill) {
        var spellType = skill.getSpell();
        if (hasSkill(skill)) return false;
        if (skill.isRoot()) return false;

        Set<Skill> unlocked = unlockedSkills.get(spellType);
        for (Holder<Skill> prereq : skill.getPrereqs()) {
            if (unlocked.contains(prereq.value())) return true;
        }

        return false;
    }

    public boolean hasSkill(Skill skill) {
        var spellType = skill.getSpell();
        if (unlockedSkills.get(spellType) == null) return false;
        return unlockedSkills.get(spellType).contains(skill) || skill.isRoot();
    }

    public boolean hasSkillReady(Skill skill) {
        return hasSkill(skill) && !cooldowns.isOnCooldown(skill);
    }

    public void addModifierWithExpiry(SpellModifier spellModifier, int expiryTicks) {
        this.timedModifiers.put(spellModifier, expiryTicks);
    }

    public Set<SpellModifier> getModifiers() {
        return this.permanentModifiers;
    }

    public void tickModifiers() {
        for (var entry : this.timedModifiers.entrySet()) {
            int i = entry.getValue();
            i--;
            if (i > 0) {
                this.timedModifiers.replace(entry.getKey(), i);
            } else {
                this.timedModifiers.remove(entry.getKey());
            }
        }
    }

    public SkillCooldowns getCooldowns() {
        return this.cooldowns;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag pathxpTag = new ListTag();
        ListTag spellXpTag = new ListTag();
        ListTag skillsTag = new ListTag();
        ListTag modifierList = new ListTag();

        for (SpellType<?> spellType : this.spellXp.keySet()) {
            CompoundTag newTag = new CompoundTag();
            newTag.putString("Spell", spellType.location().toString());
            newTag.putFloat("Xp", this.spellXp.get(spellType));
            spellXpTag.add(newTag);
        }

        for (SpellType<?> spellType : unlockedSkills.keySet()) {
            CompoundTag newTag = new CompoundTag();
            newTag.putString("Spell", spellType.location().toString());
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

        if (!this.permanentModifiers.isEmpty()) {
            for (var modifier : this.permanentModifiers) {
                CompoundTag modifierTag = new CompoundTag();
                modifierTag.putString("Modifier", modifier.id().toString());
                modifierList.add(modifierTag);
            }
        }

        tag.put("PathXp", pathxpTag);
        tag.put("SpellXp", spellXpTag);
        tag.put("Skills", skillsTag);
        tag.put("Modifiers", modifierList);
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
            ListTag skillList = tag.getList("Skills", 10);
            for (int j = 0; j < skillList.size(); j++) {
                CompoundTag nbt = skillList.getCompound(j);
                skills.add(Skill.byName(ResourceLocation.tryParse(nbt.getString("Skill"))));
            }
            this.unlockedSkills.put(SpellInit.REGISTRY.get(ResourceLocation.tryParse(tag.getString("Spell"))), skills);
        }

        if (compoundTag.contains("Modifiers", 9)) {
            ListTag modifierList = compoundTag.getList("Modifiers", 10);
            for (int i = 0; i < modifierList.size(); i++) {
                CompoundTag nbt = modifierList.getCompound(i);
                this.permanentModifiers.add(SpellModifier.getTypeFromLocation(ResourceLocation.tryParse(nbt.getString("Modifier"))));
            }
        }
    }
}
