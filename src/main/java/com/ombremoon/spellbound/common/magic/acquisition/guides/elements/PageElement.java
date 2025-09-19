package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.common.init.SBPageElements;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * This interface should be implemented by any and all elements to be used for the guide books.
 */
public interface PageElement {
    Codec<PageElement> CODEC = SBPageElements.REGISTRY
            .byNameCodec()
            .dispatch(PageElement::codec, Function.identity());

    /**
     * The method called when this element is to be rendered on the current page
     * @param graphics the GuiGraphics for renderering
     * @param leftPos the left most x coordinate for the beginning of the page
     * @param topPos the top most y coordinate for the top of the page
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     * @param partialTick the current partial tick value
     */
    void render(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick);

    /**
     * The Codec for the datapack entry for this element
     * @return MapCodec for this element
     */
    @NotNull MapCodec<? extends PageElement> codec();
}
