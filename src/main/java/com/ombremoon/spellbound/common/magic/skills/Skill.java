package com.ombremoon.spellbound.common.magic.skills;

import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.function.Supplier;

public class Skill {
    private final int xpCost;
    private final Set<Supplier<Skill>> prerequisites;
    private final ResourceLocation resourceLocation;
    private String descriptionId;

    public Skill(ResourceLocation resLoc, int xpCost, Set<Supplier<Skill>> prerequisites) {
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

    public Set<Supplier<Skill>> getPrereqs() {
        return prerequisites;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("skill", this.getId());
        }
        return this.descriptionId;
    }

    public ResourceLocation getId() {
        return SkillInit.REGISTRY.getKey(this);
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getSkillName() {
        return Component.translatable(this.getDescriptionId());
    }

    public Component getSkillDescription() {
        return Component.translatable(Util.makeDescriptionId("skill.description", this.getId()));
    }
}
