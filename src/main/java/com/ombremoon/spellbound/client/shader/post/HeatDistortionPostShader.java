package com.ombremoon.spellbound.client.shader.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.client.shader.RenderEngine;
import com.ombremoon.spellbound.main.CommonClass;
import org.jetbrains.annotations.Nullable;

public class HeatDistortionPostShader extends PostShader {
    public HeatDistortionPostShader() {
        super(CommonClass.customLocation("shaders/post/heat_distortion.json"));
    }

    @Override
    void prepShader(PoseStack poseStack) {
    }

    @Override
    public void test() {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE
        );
        this.getShaderTarget().blitToScreen(MINECRAFT.getWindow().getWidth(), MINECRAFT.getWindow().getHeight(), false);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    void cleanupShader() {
/* RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE
        );
        this.getShaderTarget().blitToScreen(MINECRAFT.getWindow().getWidth(), MINECRAFT.getWindow().getHeight(), false);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();*/
    }

    @Override
    public @Nullable RenderTarget getShaderTarget() {
        return RenderEngine.getInstance().heatDistortionTarget();
    }

    @Override
    public void initTarget() {
        RenderEngine.getInstance().heatDistortionTarget = this.postEffect.getTempTarget("final");
    }
}
