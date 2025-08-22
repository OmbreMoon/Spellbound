package com.ombremoon.spellbound.common.magic.acquisition.guides;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.GuideImage;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.GuideText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record GuideBookPage(ResourceLocation id, String insertAfter, List<GuideImage> images, List<GuideText> text) {
    public static final Codec<GuideBookPage> CODEC = RecordCodecBuilder.<GuideBookPage>create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(GuideBookPage::id),
            Codec.STRING.optionalFieldOf("insertAfter", null).forGetter(GuideBookPage::insertAfter),
            GuideImage.CODEC.listOf().optionalFieldOf("images", new ArrayList<>()).forGetter(GuideBookPage::images),
            GuideText.CODEC.listOf().optionalFieldOf("text", new ArrayList<>()).forGetter(GuideBookPage::text)
    ).apply(inst, GuideBookPage::new));

    public static final StreamCodec<FriendlyByteBuf, GuideBookPage> STREAM_CODEC = StreamCodec.ofMember(GuideBookPage::write, GuideBookPage::read);

    public static DataResult<GuideBookPage> validate(GuideBookPage page) {
        if (page.images().isEmpty() && page.text().isEmpty()) return DataResult.error(() -> "Page must include at least one image or text entry.");
        else return DataResult.success(page);
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(id);
        buffer.writeUtf(insertAfter);
        buffer.writeCollection(images, (buf, image) -> image.write(buf));
        buffer.writeCollection(text, (buf, text) -> text.write(buf));
    }

    private static GuideBookPage read(FriendlyByteBuf buffer) {
        return new GuideBookPage(
                buffer.readResourceLocation(),
                buffer.readUtf(),
                buffer.readList(GuideImage::read),
                buffer.readList(GuideText::read));
    }

    public void render(GuiGraphics graphics, int leftPos, int topPos) {
        for (GuideImage image : images) {
            image.render(graphics, leftPos + 42, topPos + 36);
        }
        for (GuideText text : text) {
            text.render(graphics, leftPos + 42, topPos + 36);
        }
    }
}
