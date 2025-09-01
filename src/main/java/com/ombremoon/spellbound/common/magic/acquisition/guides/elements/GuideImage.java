package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public record GuideImage(ResourceLocation loc, int width, int height, ElementPosition position) implements PageElement {
    public static final MapCodec<GuideImage> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("location").forGetter(GuideImage::loc),
            Codec.INT.fieldOf("width").forGetter(GuideImage::width),
            Codec.INT.fieldOf("height").forGetter(GuideImage::height),
            ElementPosition.CODEC.fieldOf("position").forGetter(GuideImage::position)
    ).apply(inst, GuideImage::new));

    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos) {
        graphics.blit(loc, leftPos + position.xOffset(), topPos + position.yOffset(), 0, 0, width, height, width, height);
    }

    @Override
    public MapCodec<? extends PageElement> codec() {
        return CODEC;
    }
}
