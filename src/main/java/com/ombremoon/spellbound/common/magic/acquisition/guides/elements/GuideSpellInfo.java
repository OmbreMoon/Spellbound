package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.ElementPosition;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.SpellInfoExtras;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.TextExtras;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import com.ombremoon.spellbound.main.Constants;
import com.sun.jna.WString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GuideSpellInfo(ResourceLocation spellLoc, SpellInfoExtras extras, ElementPosition position) implements PageElement {
    public static final MapCodec<GuideSpellInfo> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("spell").forGetter(GuideSpellInfo::spellLoc),
            SpellInfoExtras.CODEC.optionalFieldOf("extras", SpellInfoExtras.getDefault()).forGetter(GuideSpellInfo::extras),
            ElementPosition.CODEC.optionalFieldOf("position", ElementPosition.getDefault()).forGetter(GuideSpellInfo::position)
    ).apply(inst, GuideSpellInfo::new));

    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick) {
        Registry<SpellType<?>> spellRegistry = Minecraft.getInstance().level.registryAccess().registry(SBSpells.SPELL_TYPE_REGISTRY_KEY).get();
        SpellType<?> spellType = spellRegistry.get(spellLoc);
        if (spellType == null) {
            Constants.LOG.warn("Error parsing spell info. Spell {} not found in registry.", spellLoc);
            return;
        }
        AbstractSpell spell = spellType.createSpell();
        float baseDamage = spell.getBaseDamage();
        int castTime = spell.getCastTime();
        int duration = spell.getDuration();
        float manaCost = spell.getManaCost();
        float manaPerTick = 0;
        if (spell instanceof ChanneledSpell channeledSpell) {
            manaPerTick = channeledSpell.getManaTickCost();
        }

        int elementsAdded = 0;
        if (extras.mastery()) {
            drawString("spell_mastery", Component.translatable(spell.getSpellMastery().toString()), elementsAdded, leftPos, topPos, graphics);
            elementsAdded++;
        }
        if (extras.baseDamage() == 2 || (extras.baseDamage() == 1 && baseDamage > 0)) {
            drawString("damage", baseDamage, elementsAdded, leftPos, topPos, graphics);
            elementsAdded++;
        }
        if (extras.manaCost() == 2 || (extras.manaCost() == 1 && manaCost > 0)) {
            drawString("mana_cost", manaCost, elementsAdded, leftPos, topPos, graphics);
            elementsAdded++;
        }
        if (extras.castTime() == 2 || (extras.castTime() == 1 && castTime > 0)) {
            drawString("cast_time", castTime, elementsAdded, leftPos, topPos, graphics);
            elementsAdded++;
        }
        if (extras.duration() == 2 || (extras.duration() == 1 && duration > 0)) {
            drawString("duration", duration, elementsAdded, leftPos, topPos, graphics);
            elementsAdded++;
        }
        if (extras.manaPerTick() == 2 || (extras.manaPerTick() == 1 && manaPerTick > 0)) {
            drawString("mana_per_tick", manaPerTick, elementsAdded, leftPos, topPos, graphics);
            elementsAdded++;
        }

    }

    private void drawString(String key, int value, int elementCount, int leftPos, int topPos, GuiGraphics graphics) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("guide.element.spell_info." + key, value), leftPos + position.xOffset(), topPos + position.yOffset() + (elementCount * extras.lineGap()), extras.colour(), extras.dropShadow());
    }

    private void drawString(String key, Component value, int elementCount, int leftPos, int topPos, GuiGraphics graphics) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("guide.element.spell_info." + key, value), leftPos + position.xOffset(), topPos + position.yOffset() + (elementCount * extras.lineGap()), extras.colour(), extras.dropShadow());
    }

    private void drawString(String key, float value, int elementCount, int leftPos, int topPos, GuiGraphics graphics) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("guide.element.spell_info." + key, value), leftPos + position.xOffset(), topPos + position.yOffset() + (elementCount * extras.lineGap()), extras.colour(), extras.dropShadow());
    }

    @Override
    public @NotNull MapCodec<? extends PageElement> codec() {
        return null;
    }

}
