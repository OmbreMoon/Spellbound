package com.ombremoon.spellbound.client.shader.post;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Spellbound;
import com.ombremoon.spellbound.client.ClientStuff;
import com.ombremoon.spellbound.client.shader.Examples;
import net.minecraft.client.Minecraft;

public class Example2PostShader extends PostShader {
    public Example2PostShader() {
        super(CommonClass.customLocation("shaders/post/example2.json"));
    }

    @Override
    void prepShader(PoseStack poseStack) {
        Examples examples = ClientStuff.getInstance().getExamples();
        examples.exampleTarget().clear(Minecraft.ON_OSX);
        MINECRAFT.getMainRenderTarget().bindWrite(false);
        examples.exampleBufferSource().endExampleBatch();
    }

    @Override
    void cleanupShader() {
        Examples examples = ClientStuff.getInstance().getExamples();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE
        );
        examples.exampleTarget().blitToScreen(MINECRAFT.getWindow().getWidth(), MINECRAFT.getWindow().getHeight(), false);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
