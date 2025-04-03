package com.ombremoon.spellbound.client.shader.post;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

public abstract class PostShader {
    protected static final Minecraft MINECRAFT = Minecraft.getInstance();
    protected static final Logger LOGGER = Constants.LOG;
    protected PoseStack poseStack;
    private final ResourceLocation shaderLoc;
    protected PostChain postEffect;
    protected EffectInstance[] effects;
    protected boolean isActive = false;
    protected boolean init = false;

    protected PostShader(ResourceLocation shaderLoc) {
        this.shaderLoc = shaderLoc;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void toggleShader() {
        this.isActive = !this.isActive;
    }

    public final void initShader() {
        loadShader();
        this.init = true;
    }

    public final void loadShader() {
        if (this.postEffect != null) {
            this.postEffect.close();
        }

        this.postEffect = null;
        try {
            this.postEffect = new PostChain(MINECRAFT.getTextureManager(), MINECRAFT.getResourceManager(), MINECRAFT.getMainRenderTarget(), this.shaderLoc);
            this.postEffect.resize(MINECRAFT.getWindow().getWidth(), MINECRAFT.getWindow().getHeight());
            initTarget();
        } catch (IOException e) {
            LOGGER.warn("Failed to load shader: {}", this.shaderLoc, e);
        } catch (JsonSyntaxException jsonSyntaxException) {
            LOGGER.warn("Failed to parse shader: {}", this.shaderLoc, jsonSyntaxException);
        }
    }

    @Nullable
    public RenderTarget getShaderTarget() {
        return null;
    }

    public void initTarget() {
    }

    public void resize(int width, int height) {
        if (this.postEffect != null) {
            this.postEffect.resize(width, height);
        }
    }

    public final void processShader() {
        if (this.isActive) {
            if (!this.init)
                initShader();

            if (this.postEffect != null) {
                //apply uniforms
                prepShader(poseStack);
                this.postEffect.process(MINECRAFT.getTimer().getGameTimeDeltaTicks());
                LOGGER.info("{}", this.shaderLoc);
                test();
                MINECRAFT.getMainRenderTarget().bindWrite(false);
                cleanupShader();
            }
        }
    }

    public void test() {}

    abstract void prepShader(PoseStack poseStack);

    abstract void cleanupShader();

    public void setupShaderStack(PoseStack clientStack) {
        this.poseStack = clientStack;
    }
}
