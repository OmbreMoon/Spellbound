package com.ombremoon.spellbound.common.content.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class Skill {
    private final int xpCost;
    private final Set<Skill> prerequisites;
    private final ResourceLocation resourceLocation;

    public Skill(ResourceLocation resLoc, int xpCost, Set<Skill> prerequisites) {
        this.xpCost = xpCost;
        this.resourceLocation = resLoc;
        this.prerequisites = prerequisites;
    }

    public int getXpCost() {
        return xpCost;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public Set<Skill> getPrereqs() {
        return prerequisites;
    }

    public Component getName() {
        return Component.translatable("skill_name." + resourceLocation.getNamespace() + "." + resourceLocation.getPath());
    }

    public Component getDescription() {
        return Component.translatable("skill_description." + resourceLocation.getNamespace() + "." + resourceLocation.getPath());
    }
}
