package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.ElementPosition;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.TextListExtras;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record GuideTextList(List<String> list, TextListExtras extras, ElementPosition position) implements PageElement {
    public static final MapCodec<GuideTextList> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.listOf().fieldOf("list").forGetter(GuideTextList::list),
            TextListExtras.CODEC.optionalFieldOf("extras", TextListExtras.getDefault()).forGetter(GuideTextList::extras),
            ElementPosition.CODEC.optionalFieldOf("position", ElementPosition.getDefault()).forGetter(GuideTextList::position)
    ).apply(inst, GuideTextList::new));

    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick) {
        for (int i = 0; i < list.size(); i++) {

            int maxRows = extras.maxRows();
            int xOffset;
            int yOffset;
            if (maxRows <= 0) {
                xOffset = 0;
                yOffset = i * 20;
            } else {
                xOffset = i >= maxRows ? Math.floorDiv(i, maxRows) * extras.columnGap() : 0;
                yOffset = (i >= maxRows ? (i % maxRows) : i) * extras.rowGap();
            }

            graphics.drawString(Minecraft.getInstance().font,
                    Component.literal(extras.bulletPoint()).append(Component.translatable(list.get(i))),
                    leftPos - 10 + position.xOffset() + xOffset,
                    topPos + position.yOffset() + 6 + yOffset,
                    extras.textColour(),
                    extras.dropShadow()
            );
        }
    }

    @Override
    public @NotNull MapCodec<? extends PageElement> codec() {
        return CODEC;
    }
}
