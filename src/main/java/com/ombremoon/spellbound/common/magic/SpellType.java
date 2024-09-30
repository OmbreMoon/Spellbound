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
    private final Set<Holder<Skill>> availableSkills;

    public SpellType(ResourceLocation resourceLocation, SpellPath path, Set<Holder<Skill>> skills, SpellFactory<S> factory) {
        this.path = path;
        this.availableSkills = skills;
        this.factory = factory;
        this.resourceLocation = resourceLocation;

//        if (this.getRootSkill() == null) throw new IllegalStateException(this + " does not contain a root skill");
    }

    public ResourceLocation location() {
        return this.resourceLocation;
    }

    public SpellPath getPath() {
        return this.path;
    }

    public Skill getRootSkill() {
        return this.availableSkills.isEmpty() ? null : this.getSkills().getFirst().getRoot();
    }

    public List<Skill> getSkills() {
        return availableSkills.stream().map(Holder::value).collect(Collectors.toList());
    }

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
        private Set<Holder<Skill>> availableSkills = new ObjectOpenHashSet<>();

        public Builder(String resourceLocation, SpellFactory<T> factory) {
            this.resourceLocation = CommonClass.customLocation(resourceLocation);
            this.factory = factory;
        }

        public Builder<T> setPath(SpellPath path) {
            this.path = path;
            return this;
        }

        @SafeVarargs
        public final Builder<T> skills(Holder<Skill>... skills) {
            this.availableSkills.addAll(Arrays.stream(skills).toList());
            return this;
        }

        public SpellType<T> build() {
            return new SpellType<>(resourceLocation, path, availableSkills, factory);
        }
    }
}
