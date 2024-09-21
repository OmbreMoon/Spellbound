package com.ombremoon.spellbound.common.magic.tree;

import com.ombremoon.spellbound.common.magic.skills.Skill;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class SkillNode {
    private final Skill skill;
    @Nullable
    private final Set<SkillNode> parents;
    private final Set<SkillNode> children = new ReferenceOpenHashSet<>();

    public SkillNode(Skill skill, @Nullable Set<SkillNode> parents) {
        this.skill = skill;
        this.parents = parents;
    }

    public Skill skill() {
        return this.skill;
    }

    @Nullable
    public Set<SkillNode> parents() {
        return this.parents;
    }

    public SkillNode root() {
        return getRoot(this);
    }

    public static SkillNode getRoot(SkillNode node) {
        SkillNode skillNode = node;

        while (true) {
            var prevNodes = node.parents();
            if (prevNodes == null) {
                return skillNode;
            }

            skillNode = prevNodes.iterator().next();
        }
    }

    public Iterable<SkillNode> children() {
        return this.children;
    }

    public void addChild(SkillNode child) {
        this.children.add(child);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof SkillNode skillNode && this.skill.equals(skillNode.skill());
        }
    }

    @Override
    public int hashCode() {
        return this.skill.hashCode();
    }

    @Override
    public String toString() {
        return this.skill.location().toString();
    }
}
