package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.common.magic.skills.Skill;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Set;

public class SpellType<S extends AbstractSpell> {
    private final ResourceLocation resourceLocation;
    private final SpellFactory<S> factory;
    private final SpellPath path;
    private final Set<Skill> availableSkills;

    public SpellType(ResourceLocation resourceLocation, SpellPath path, Set<Skill> skills, SpellFactory<S> factory) {
        this.path = path;
        this.availableSkills = skills;
        this.factory = factory;
        this.resourceLocation = resourceLocation;
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }

    public SpellPath getPath() {
        return this.path;
    }

    public Set<Skill> getSkills() {
        return availableSkills;
    }

    @Nullable
    public S createSpell() {
        return this.factory.create();
    }

    public interface SpellFactory<S extends AbstractSpell> {
        S create();
    }

    public class Builder {
        private ResourceLocation resourceLocation;
        private SpellFactory<S> factory;
        private SpellPath path;
        private Set<Skill> availableSkills;

        public Builder(ResourceLocation resourceLocation, SpellFactory<S> factory) {
            this.resourceLocation = resourceLocation;
            this.factory = factory;
        }

        public Builder setPath(SpellPath path) {
            this.path = path;
            return this;
        }

        public Builder setAvailableSkills(Set<Skill> skills) {
            this.availableSkills = skills;
            return this;
        }

        public SpellType<S> build() {
            return new SpellType<>(resourceLocation, path, availableSkills, factory);
        }
    }
}
