package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.ElementPosition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GuideImage(ResourceLocation loc, int width, int height, ElementPosition position) implements PageElement {
    public static final MapCodec<GuideImage> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("image").forGetter(GuideImage::loc),
            Codec.INT.fieldOf("width").forGetter(GuideImage::width),
            Codec.INT.fieldOf("height").forGetter(GuideImage::height),
            ElementPosition.CODEC.fieldOf("position").forGetter(GuideImage::position)
    ).apply(inst, GuideImage::new));

    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick) {
        graphics.blit(loc, leftPos + position.xOffset(), topPos + position.yOffset(), 0, 0, width, height, width, height);
    }

    @Override
    public @NotNull MapCodec<? extends PageElement> codec() {
        return CODEC;
    }
}
