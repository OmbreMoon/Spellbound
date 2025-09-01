package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.jcraft.jogg.Page;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public record GuideText(String translationKey, int colour, ElementPosition position) implements PageElement {
    public static final MapCodec<GuideText> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("translation").forGetter(GuideText::translationKey),
            Codec.INT.optionalFieldOf("colour", 16777215).forGetter(GuideText::colour),
            ElementPosition.CODEC.fieldOf("position").forGetter(GuideText::position)
    ).apply(inst, GuideText::new));

    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable(translationKey), leftPos + position.xOffset(), topPos + position.yOffset(), colour);
    }

    @Override
    public MapCodec<? extends PageElement> codec() {
        return CODEC;
    }
}
