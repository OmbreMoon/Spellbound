package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpellType<S extends AbstractSpell> {
    private final ResourceLocation resourceLocation;
    private final SpellFactory<S> factory;
    private final SpellPath path;
    private final Holder<Skill> keySkill;
    private final Set<Holder<Skill>> availableSkills;

    public SpellType(ResourceLocation resourceLocation, SpellPath path, Set<Holder<Skill>> skills, Holder<Skill> keySkill, SpellFactory<S> factory) {
        this.path = path;
        this.availableSkills = skills;
        this.factory = factory;
        this.resourceLocation = resourceLocation;
        this.keySkill = keySkill;
    }

    public ResourceLocation location() {
        return this.resourceLocation;
    }

    public SpellPath getPath() {
        return this.path;
    }

    public Holder<Skill> getKeySkill() {
        return this.keySkill;
    }

    public List<Skill> getSkills() {
        return availableSkills.stream().map(Holder::value).collect(Collectors.toList());
    }

    @Nullable
    public S createSpell() {
        return this.factory.create();
    }

    public interface SpellFactory<S extends AbstractSpell> {
        S create();
    }

    @Override
    public String toString() {
        return this.resourceLocation.toString();
    }

    @Override
    public int hashCode() {
        return this.resourceLocation.hashCode();
    }

    public static class Builder<T extends AbstractSpell>{
        private ResourceLocation resourceLocation;
        private SpellFactory<T> factory;
        private SpellPath path;
        private Holder<Skill> keySkill;
        private Set<Holder<Skill>> availableSkills = new ObjectOpenHashSet<>();

        public Builder(String resourceLocation, SpellFactory<T> factory) {
            this.resourceLocation = CommonClass.customLocation(resourceLocation);
            this.factory = factory;
        }

        public Builder<T> setPath(SpellPath path) {
            this.path = path;
            return this;
        }

        public Builder<T> setKeySkill(Holder<Skill> skill) {
            this.availableSkills.add(skill);
            this.keySkill = skill;
            return this;
        }

        public Builder<T> setAvailableSkills(Holder<Skill>... skills) {
            this.availableSkills.addAll(Arrays.stream(skills).toList());
            return this;
        }

        public SpellType<T> build() {
            return new SpellType<>(resourceLocation, path, availableSkills, keySkill, factory);
        }
    }
}
