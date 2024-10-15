package com.ombremoon.spellbound.common.magic.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;

public class SkillNode {
    private static final Logger LOGGER = Constants.LOG;
    private final Skill skill;
    private final List<SkillNode> parents;
    private final Set<SkillNode> children = new ReferenceOpenHashSet<>();

    public static final Codec<SkillNode> CODEC = Codec.recursive(
            SkillNode.class.getSimpleName(),
            codec -> RecordCodecBuilder.create(instance -> instance.group(
                    SBSkills.REGISTRY.byNameCodec().fieldOf("skill").forGetter(SkillNode::skill),
                    codec.listOf().fieldOf("parents").forGetter(SkillNode::parents)
            ).apply(instance, SkillNode::new))
    );

    public SkillNode(Skill skill, List<SkillNode> parents) {
        this.skill = skill;
        this.parents = parents;
    }

    public Skill skill() {
        return this.skill;
    }

    public List<SkillNode> parents() {
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

            skillNode = prevNodes.getFirst();
        }
    }

    public Iterable<SkillNode> children() {
        return this.children;
    }

    public void addChild(SkillNode child) {
        this.children.add(child);
    }

    public Tag save() {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    @Nullable
    public static SkillNode load(CompoundTag nbt) {
        return CODEC.parse(NbtOps.INSTANCE, nbt).resultOrPartial(LOGGER::error).orElse(null);
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
