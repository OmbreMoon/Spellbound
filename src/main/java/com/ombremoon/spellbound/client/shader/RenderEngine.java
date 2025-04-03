package com.ombremoon.spellbound.client.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.jetbrains.annotations.Nullable;

public class RenderEngine {
    static RenderEngine instance;
    @Nullable
    public RenderTarget heatDistortionTarget;

    public RenderEngine() {
        instance = this;
    }

    public @Nullable RenderTarget heatDistortionTarget() {
        return this.heatDistortionTarget;
    }

    public static RenderEngine getInstance() {
        if (instance == null)
            instance = new RenderEngine();

        return instance;
    }
}
