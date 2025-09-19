package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.ElementPosition;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.TextExtras;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record GuideText(String translationKey, TextExtras extras, ElementPosition position) implements PageElement {
    public static final MapCodec<GuideText> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("translation").forGetter(GuideText::translationKey),
            TextExtras.CODEC.optionalFieldOf("extras", TextExtras.getDefault()).forGetter(GuideText::extras),
            ElementPosition.CODEC.optionalFieldOf("position", ElementPosition.getDefault()).forGetter(GuideText::position)
    ).apply(inst, GuideText::new));

    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;

        List<FormattedCharSequence> lines = font.split(Component.translatable(translationKey), extras.maxLineLength());
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), leftPos + position.xOffset(), topPos + position.yOffset() + (i * 9), extras.colour(), extras.dropShadow());
        }
    }

    @Override
    public @NotNull MapCodec<? extends PageElement> codec() {
        return CODEC;
    }
}
