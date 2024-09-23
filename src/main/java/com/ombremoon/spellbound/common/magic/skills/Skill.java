package com.ombremoon.spellbound.common.magic.skills;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Skill {
    private final ResourceLocation resourceLocation;
    private final int xpCost;
    private final int xPos;
    private final int yPos;
    @Nullable
    private final HolderSet<Skill> prerequisites;
    private String descriptionId;

    public Skill(ResourceLocation resLoc) {
        this(resLoc, 0, 0, 0, null);
    }

    public Skill(ResourceLocation resLoc, int xpCost, int xPos, int yPos, @Nullable HolderSet<Skill> prerequisites) {
        this.resourceLocation = resLoc;
        this.xpCost = xpCost;
        this.xPos = xPos;
        this.yPos = yPos;
        this.prerequisites = prerequisites;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public int getXpCost() {
        return xpCost;
    }

    public int getX() {
        return this.xPos;
    }

    public int getY() {
        return this.yPos;
    }

    public HolderSet<Skill> getPrereqs() {
        return prerequisites;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("skill", this.location());
        }
        return this.descriptionId;
    }

    public ResourceLocation location() {
        return SkillInit.REGISTRY.getKey(this);
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getSkillName() {
        return Component.translatable(this.getDescriptionId());
    }

    public MutableComponent getSkillDescription() {
        return Component.translatable(Util.makeDescriptionId("skill.description", this.location()));
    }

    public ResourceLocation getSkillTexture() {
        String root = getSpell().location().getPath();
        return CommonClass.customLocation("textures/gui/skills/" + root + "/" + location().getPath() + ".png");
    }

    public boolean isRoot() {
        return this.prerequisites == null;
    }

    public static Skill getRoot(Skill skill) {
        Skill root = skill;
        while (true) {
            var prereqs = root.getPrereqs();
            if (prereqs == null) {
                return root;
            }
            root = prereqs.stream().map(Holder::value).toList().getFirst();
        }
    }

    public static Skill byName(ResourceLocation resourceLocation) {
        return SkillInit.REGISTRY.get(resourceLocation);
    }

    public SpellType<?> getSpell() {
        return AbstractSpell.getSpellByName(CommonClass.customLocation(getRoot(this).location().getPath()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Skill skill && this.location().equals(skill.location());
    }

    @Override
    public int hashCode() {
        return this.location().hashCode();
    }

    @Override
    public String toString() {
        return this.location().toString();
    }
}
