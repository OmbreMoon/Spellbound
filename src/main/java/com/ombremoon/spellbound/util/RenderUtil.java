package com.ombremoon.spellbound.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.ombremoon.spellbound.client.gui.GuideBookScreen;
import com.ombremoon.spellbound.client.gui.WorkbenchScreen;
import com.ombremoon.spellbound.client.renderer.SBRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.Optional;

public class RenderUtil {

    public static void setupScreen(ResourceLocation resourceLocation) {
        setupScreen(resourceLocation, 1.0F);
    }

    public static void setupScreen(ResourceLocation resourceLocation, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.setShaderTexture(0, resourceLocation);
    }

    public static int getScaledRender(float current, int max, int size) {
        return max != 0 && current != 0 ? (int) (current * size / max) : 0;
    }

    public static void drawWordWrap(GuiGraphics guiGraphics, Font font, FormattedText text, int x, int y, int lineWidth, int color) {
        for (FormattedCharSequence formattedcharsequence : font.split(text, lineWidth)) {
            drawCenteredString(guiGraphics, font, formattedcharsequence, x, y, color);
            y += 9;
        }
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        guiGraphics.drawString(font, text, x - font.width(text) / 2, y - font.lineHeight / 2, color);
    }

    public static void openWorkbench() {
        Minecraft.getInstance().setScreen(new WorkbenchScreen(Component.translatable("screen.spellbound.workbench")));
    }

    public static void openBook(ResourceLocation id) {
        Minecraft.getInstance().setScreen(new GuideBookScreen(Component.translatable("screen.spellbound.guide_book"), id));
    }

/*    public static Optional<RenderType> getExampleRenderType(RenderType renderType) {
        if (renderType instanceof RenderType.CompositeRenderType compositeRenderType) {
            if (compositeRenderType.state.textureState instanceof RenderStateShard.TextureStateShard shard) {
                return compositeRenderType.state.outlineProperty == RenderType.OutlineProperty.AFFECTS_OUTLINE
                        ? shard.texture.map(location -> SBRenderTypes.EXAMPLE.apply(location, compositeRenderType.state.cullState))
                        : Optional.empty();
            }
        }
        return Optional.empty();
    }*/
}
