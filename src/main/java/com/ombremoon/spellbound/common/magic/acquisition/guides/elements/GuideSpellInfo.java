package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.datafixers.util.Pair;
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

import java.util.ArrayList;
import java.util.List;

public record GuideSpellInfo(ResourceLocation spellLoc, SpellInfoExtras extras, ElementPosition position) implements PageElement {
    private static final ResourceLocation DATA_SPRITE = ResourceLocation.withDefaultNamespace("advancements/title_box");
    private static final ResourceLocation TITLE_BOX_SPRITE = ResourceLocation.withDefaultNamespace("advancements/box_unobtained");

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
        AbstractSpell spell = spellType.createSpellWithData(Minecraft.getInstance().player);
        float baseDamage = spell.getBaseDamage();
        int castTime = spell.getCastTime();
        int duration = spell.getDuration();
        float manaCost = spell.getManaCost();
        float manaPerTick = 0;
        if (spell instanceof ChanneledSpell channeledSpell) {
            manaPerTick = channeledSpell.getManaTickCost();
        }

        List<Pair<String, Object>> data = new ArrayList<>();

        if (extras.mastery()) {
            data.add(Pair.of("spell_mastery", Component.translatable(spell.getSpellMastery().toString())));
        }
        if (extras.baseDamage() == 2 || (extras.baseDamage() == 1 && baseDamage > 0)) {
            data.add(Pair.of("damage", baseDamage));
        }
        if (extras.manaCost() == 2 || (extras.manaCost() == 1 && manaCost > 0)) {
            data.add(Pair.of("mana_cost", manaCost));
        }
        if (extras.castTime() == 2 || (extras.castTime() == 1 && castTime > 0)) {
            data.add(Pair.of("cast_time", castTime));
        }
        if (extras.duration() == 2 || (extras.duration() == 1 && duration > 0)) {
            data.add(Pair.of("duration", duration));
        }
        if (extras.manaPerTick() == 2 || (extras.manaPerTick() == 1 && manaPerTick > 0)) {
            data.add(Pair.of("mana_per_tick", manaPerTick));
        }

        graphics.blitSprite(DATA_SPRITE, leftPos + position.xOffset(), topPos + position.yOffset(), 150, data.size() * (extras.lineGap() + 4));
        graphics.blitSprite(TITLE_BOX_SPRITE, leftPos + position.xOffset(), topPos + position.yOffset(), 150, 17);
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("guide.element.spell_info"), leftPos + position.xOffset() + 4, topPos + 5 + position.yOffset(), extras.colour(), extras.dropShadow());

        for (int i = 0; i < data.size(); i++) {
            Pair<String, Object> pair = data.get(i);
            if (pair.getSecond() instanceof Component value) drawString(pair.getFirst(), value, i, leftPos, topPos, graphics);
            else if (pair.getSecond() instanceof Integer value) drawString(pair.getFirst(), value, i, leftPos, topPos, graphics);
            else if (pair.getSecond() instanceof Float value) drawString(pair.getFirst(), value, i, leftPos, topPos, graphics);
        }

    }

    private void drawString(String key, int value, int elementCount, int leftPos, int topPos, GuiGraphics graphics) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("guide.element.spell_info." + key, value), leftPos + position.xOffset() + 4, topPos + 17 +position.yOffset() + (elementCount * extras.lineGap()), extras.colour(), extras.dropShadow());
    }

    private void drawString(String key, Component value, int elementCount, int leftPos, int topPos, GuiGraphics graphics) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("guide.element.spell_info." + key, value), leftPos + position.xOffset() + 4, topPos + 17 + position.yOffset() + (elementCount * extras.lineGap()), extras.colour(), extras.dropShadow());
    }

    private void drawString(String key, float value, int elementCount, int leftPos, int topPos, GuiGraphics graphics) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("guide.element.spell_info." + key, value), leftPos + position.xOffset() + 4, topPos + 17 + position.yOffset() + (elementCount * extras.lineGap()), extras.colour(), extras.dropShadow());
    }

    @Override
    public @NotNull MapCodec<? extends PageElement> codec() {
        return CODEC;
    }

}
