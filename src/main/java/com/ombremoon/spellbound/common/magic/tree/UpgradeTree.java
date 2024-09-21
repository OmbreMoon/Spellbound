package com.ombremoon.spellbound.common.magic.tree;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class UpgradeTree {
    private static final Logger LOGGER = Constants.LOG;
    private final Map<ResourceLocation, SkillNode> nodes = new Object2ObjectOpenHashMap<>();
    private final Set<SkillNode> roots = new ObjectLinkedOpenHashSet<>();
    private final Set<SkillNode> children = new ObjectLinkedOpenHashSet<>();

    private void remove(SkillNode node) {
        for (SkillNode skillNode : node.children()) {
            this.remove(skillNode);
        }

        LOGGER.info("Forgot about upgrade {}", node.skill());
        this.nodes.remove(node.skill().location());
        if (node.parents() == null) {
            this.roots.remove(node);
        } else {
            this.children.remove(node);
        }
    }

    public void remove(Set<ResourceLocation> skills) {
        for (ResourceLocation location : skills) {
            SkillNode node = this.nodes.get(location);
            if (node == null) {
                LOGGER.warn("Told to remove upgrade {} but does not exist", location);
            } else {
                this.remove(node);
            }
        }
    }

    public void addAll(Collection<Skill> skills) {
        List<Skill> list = new ArrayList<>(skills);

        while (!list.isEmpty()) {
            if (!list.removeIf(this::addNode)) {
                LOGGER.error("Couldn't load skills: {}", list);
                break;
            }
        }

        LOGGER.info("Loaded {} skills", this.nodes.size());
    }

    private boolean addNode(Skill skill) {
        final Set<SkillNode> nodes = new ObjectLinkedOpenHashSet<>();
        HolderSet<Skill> parents = skill.getPrereqs();
        if (parents != null) {
            var locs = parents.stream().map(Holder::value).map(Skill::location).collect(Collectors.toSet());
            for (var loc : locs) {
                SkillNode skillNode = this.nodes.get(loc);
                if (skillNode == null && loc != null) return false;
                nodes.add(skillNode);
            }
        }

        SkillNode skillNode = new SkillNode(skill, nodes);
        nodes.forEach(node -> {
            if (node != null) node.addChild(skillNode);
        });

        this.nodes.put(skill.location(), skillNode);
        if (nodes.isEmpty()) {
            this.roots.add(skillNode);
        } else {
            this.children.add(skillNode);
        }
        return true;
    }

    public void clear() {
        this.nodes.clear();
        this.roots.clear();
        this.children.clear();
    }

    public Set<SkillNode> roots() {
        return this.roots;
    }

    public Set<SkillNode> children() {
        return this.children;
    }

    public Collection<SkillNode> nodes() {
        return this.nodes.values();
    }

    public SkillNode get(ResourceLocation location) {
        return this.nodes.get(location);
    }

    public SkillNode get(Skill skill) {
        return this.nodes.get(skill.location());
    }
}
