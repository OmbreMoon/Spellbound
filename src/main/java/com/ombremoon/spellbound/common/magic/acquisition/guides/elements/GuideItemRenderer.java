package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public record GuideItemRenderer() implements PageElement {
    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public @NotNull MapCodec<? extends PageElement> codec() {
        return null;
    }
}
