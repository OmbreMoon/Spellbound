package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public record GuideText(String translationKey, ElementPosition position) {
    public static final Codec<GuideText> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("translation").forGetter(GuideText::translationKey),
            ElementPosition.CODEC.fieldOf("position").forGetter(GuideText::position)
    ).apply(inst, GuideText::new));

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(translationKey);
        position.write(buf);
    }

    public static GuideText read(FriendlyByteBuf buffer) {
        return new GuideText(buffer.readUtf(), ElementPosition.read(buffer));
    }

    public void render(GuiGraphics graphics, int leftPos, int topPos) {
        graphics.drawString(Minecraft.getInstance().font, Component.translatable(translationKey), leftPos + position.xOffset(), topPos + position.yOffset(), 16777215);
    }
}
