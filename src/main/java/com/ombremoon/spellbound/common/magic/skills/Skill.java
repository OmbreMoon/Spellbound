package com.ombremoon.spellbound.common.magic.skills;

import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.Util;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Skill {
    private final Supplier<? extends SpellType<?>> spellType;
    private final int xpCost;
    @Nullable
    private final HolderSet<Skill> prerequisites;
    private final ResourceLocation resourceLocation;
    private String descriptionId;

    public Skill(Supplier<? extends SpellType<?>> spellType) {
        this(spellType, null, 0, null);
    }

    public Skill(Supplier<? extends SpellType<?>> spellType, ResourceLocation resLoc, int xpCost, @Nullable  HolderSet<Skill> prerequisites) {
        this.spellType = spellType;
        this.xpCost = xpCost;
        this.resourceLocation = resLoc;
        this.prerequisites = prerequisites;
    }

    public SpellType<?> getSpell() {
        return this.spellType.get();
    }

    public int getXpCost() {
        return xpCost;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
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

    public Component getSkillDescription() {
        return Component.translatable(Util.makeDescriptionId("skill.description", this.location()));
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
        return  this.location().toString();
    }
}
