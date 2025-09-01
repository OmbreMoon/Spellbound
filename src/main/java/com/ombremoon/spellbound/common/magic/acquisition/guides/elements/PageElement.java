package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.common.init.SBPageElements;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

public interface PageElement {
    Codec<PageElement> CODEC = SBPageElements.REGISTRY
            .byNameCodec()
            .dispatch(PageElement::codec, Function.identity());

    void render(GuiGraphics graphics, int leftPos, int topPos);

    MapCodec<? extends PageElement> codec();
}
