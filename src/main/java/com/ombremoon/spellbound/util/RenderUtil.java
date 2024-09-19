package com.ombremoon.spellbound.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

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
}
