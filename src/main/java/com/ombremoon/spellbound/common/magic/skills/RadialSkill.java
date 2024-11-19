package com.ombremoon.spellbound.common.magic.skills;

import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class RadialSkill extends Skill {
    private final int skillFlag;

    public RadialSkill(ResourceLocation resLoc, int skillFlag) {
        super(resLoc);
        this.skillFlag = skillFlag;
    }

    public RadialSkill(ResourceLocation resLoc, int xPos, int yPos, @Nullable HolderSet<Skill> prerequisites, int skillFlag) {
        super(resLoc, xPos, yPos, prerequisites);
        this.skillFlag = skillFlag;
    }

    public int getSkillFlag() {
        return this.skillFlag;
    }
}
