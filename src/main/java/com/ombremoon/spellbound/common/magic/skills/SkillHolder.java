package com.ombremoon.spellbound.common.magic.skills;

import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.buff.SpellModifier;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.main.ConfigHandler;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.networking.PayloadHandler;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SkillHolder implements INBTSerializable<CompoundTag> {
    public static final int MAX_SPELL_LEVEL = ConfigHandler.COMMON.maxSpellLevel.get();
    public static final boolean REQUIRES_PREREQS = ConfigHandler.COMMON.skillRequiresPrereqs.get();
    private LivingEntity caster;
    protected final Map<SpellPath, Float> pathXp = new Object2FloatOpenHashMap<>();
    protected final Map<SpellType<?>, Float> spellXp = new Object2FloatOpenHashMap<>();
    public final Map<SpellType<?>, Set<Skill>> unlockedSkills = new Object2ObjectOpenHashMap<>();
    private final Set<SpellModifier> permanentModifiers = new ObjectOpenHashSet<>();
    private final Set<SpellModifier> timedModifiers = new ObjectOpenHashSet<>();
    private final SkillCooldowns cooldowns = new SkillCooldowns();

    public void sync() {
        if (this.caster instanceof Player player)
            PayloadHandler.syncSkillsToClient(player);
    }

    public void init(LivingEntity caster) {
        this.caster = caster;
    }

    public void resetSpellXP(SpellType<?> spellType) {
        spellXp.put(spellType, 0f);
    }

    public int getPathLevel(SpellPath path) {
        int level = this.getLevelFromXP(getPathXp(path));
        return Math.min(level, MAX_SPELL_LEVEL);
    }

    public float getPathXp(SpellPath path) {
        return Math.min(pathXp.getOrDefault(path, 0F), MAX_SPELL_LEVEL * 100);
    }

    public int getSpellLevel(SpellType<?> spellType) {
        int level = this.getLevelFromXP(getSpellXp(spellType));
        return Math.min(level, MAX_SPELL_LEVEL);
    }

    public float getSpellXp(SpellType<?> spellType) {
        return spellXp.getOrDefault(spellType, 0F);
    }

    public void awardSpellXp(SpellType<?> spellType, float xp) {
        spellXp.put(spellType, Math.min(getSpellXp(spellType) + xp, MAX_SPELL_LEVEL * 100));
        pathXp.put(spellType.getPath(), getPathXp(spellType.getPath()) + (xp / 2));
    }

    public <T extends AbstractSpell> void resetSkills(SpellType<T> spellType) {
        this.unlockedSkills.put(spellType, new HashSet<>() {{
            add(spellType.getRootSkill());
        }});
        for (Skill skill : spellType.getSkills()) {
            if (skill instanceof ModifierSkill modifierSkill) {
                var modifiers = modifierSkill.getModifiers();
                modifiers.forEach(permanentModifiers::remove);
            }
        }
    }

    public void unlockSkill(Skill skill) {
        Set<Skill> unlocked = unlockedSkills.get(skill.getSpell());
        if (unlocked == null) unlocked = new HashSet<>();
        unlocked.add(skill);
        unlockedSkills.put(skill.getSpell(), unlocked);

        if (caster instanceof Player player)
            skill.onSkillUnlock(player);

        if (skill instanceof ModifierSkill modifierSkill)
            permanentModifiers.addAll(modifierSkill.getModifiers());
    }

    public boolean canUnlockSkill(Skill skill) {
        var spellType = skill.getSpell();
        if (hasSkill(skill)) return false;
        if (skill.isRoot() || skill.getPrereqs() == null) return false;

        Set<Skill> unlocked = unlockedSkills.get(spellType);
        if (unlocked == null) return false;
        if (unlocked.size() >= MAX_SPELL_LEVEL) return false;
        if (!skill.canUnlockSkill((Player) this.caster, this)) return false;

        if (!REQUIRES_PREREQS) return true;

        for (Holder<Skill> preReq : skill.getPrereqs()) {
            if (unlocked.contains(preReq.value())) return true;
        }

        return false;
    }

    public boolean hasSkill(Holder<Skill> skill) {
        return hasSkill(skill.value());
    }

    public boolean hasSkill(Skill skill) {
        var spellType = skill.getSpell();
        if (unlockedSkills.get(spellType) == null) return false;
        return unlockedSkills.get(spellType).contains(skill) || skill.isRoot();
    }

    public boolean hasSkillReady(Skill skill) {
        return hasSkill(skill) && !cooldowns.isOnCooldown(skill);
    }

    public void addModifierWithExpiry(SpellModifier spellModifier) {
        this.timedModifiers.add(spellModifier);
    }

    public void removeModifier(SpellModifier spellModifier) {
        this.timedModifiers.remove(spellModifier);
        this.permanentModifiers.remove(spellModifier);
    }

    public Set<SpellModifier> getModifiers() {
        var modifiers = new ObjectOpenHashSet<>(this.permanentModifiers);
        modifiers.addAll(this.timedModifiers);
        return modifiers;
    }

    public void clearModifiers() {
        this.permanentModifiers.clear();
    }

    public SkillCooldowns getCooldowns() {
        return this.cooldowns;
    }

    private int getLevelFromXP(float xp) {
        double level = (-1 + Math.sqrt(1 + 4 * (xp / 50.0))) / 2;
        return (int) Math.floor(level);
    }

    public int getXPGoal(int level) {
        return 100 * (level * (level + 1)) / 2;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag pathxpTag = new ListTag();
        ListTag spellXpTag = new ListTag();
        ListTag skillsTag = new ListTag();
        ListTag modifierList = new ListTag();

        for (SpellType<?> spellType : this.spellXp.keySet()) {
            if (spellType == null) {
                this.spellXp.remove(null);
                continue;
            }
            CompoundTag newTag = new CompoundTag();
            newTag.putString("Spell", spellType.location().toString());
            newTag.putFloat("Xp", this.spellXp.get(spellType));
            spellXpTag.add(newTag);
        }

        for (SpellType<?> spellType : unlockedSkills.keySet()) {
            if (spellType == null) {
                this.unlockedSkills.remove(null);
                continue;
            }
            CompoundTag newTag = new CompoundTag();
            newTag.putString("Spell", spellType.location().toString());
            ListTag savedSkills = new ListTag();
            for (Skill skill : unlockedSkills.get(spellType)) {
                if (skill == null) continue;
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
            this.spellXp.put(SBSpells.REGISTRY.get(ResourceLocation.tryParse(tag.getString("Spell"))), tag.getFloat("Xp"));
        }

        for (int i = 0; i < skillTag.size(); i++) {
            CompoundTag tag = skillTag.getCompound(i);
            Set<Skill> skills = new HashSet<>();
            ListTag skillList = tag.getList("Skills", 10);
            for (int j = 0; j < skillList.size(); j++) {
                CompoundTag nbt = skillList.getCompound(j);
                skills.add(Skill.byName(ResourceLocation.tryParse(nbt.getString("Skill"))));
            }
            this.unlockedSkills.put(SBSpells.REGISTRY.get(ResourceLocation.tryParse(tag.getString("Spell"))), skills);
        }

        if (compoundTag.contains("Modifiers", 9)) {
            this.permanentModifiers.clear();
            ListTag modifierList = compoundTag.getList("Modifiers", 10);
            for (int i = 0; i < modifierList.size(); i++) {
                CompoundTag nbt = modifierList.getCompound(i);
                SpellModifier modifier = SpellModifier.getTypeFromLocation(ResourceLocation.tryParse(nbt.getString("Modifier")));
                if (modifier != null)
                    this.permanentModifiers.add(modifier);

            }
        }
    }
}
