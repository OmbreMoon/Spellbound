package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.ElementPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record GuideText(String translationKey, int colour, ElementPosition position) implements PageElement {
    public static final MapCodec<GuideText> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("translation").forGetter(GuideText::translationKey),
            Codec.INT.optionalFieldOf("colour", 16777215).forGetter(GuideText::colour),
            ElementPosition.CODEC.fieldOf("position").forGetter(GuideText::position)
    ).apply(inst, GuideText::new));

    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable(translationKey), leftPos + position.xOffset(), topPos + position.yOffset(), colour);
    }

    @Override
    public @NotNull MapCodec<? extends PageElement> codec() {
        return CODEC;
    }
}
