package com.ombremoon.spellbound.client.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;

public class Examples {
    private final MultiBufferSource.BufferSource bufferSource;
    private final ExampleBufferSource exampleBufferSource;
    @Nullable
    private RenderTarget exampleTarget;

    public Examples() {
        this.bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        this.exampleBufferSource = new ExampleBufferSource(this.bufferSource);
    }

    public MultiBufferSource.BufferSource getBufferSource() {
        return this.bufferSource;
    }

    public ExampleBufferSource exampleBufferSource() {
        return this.exampleBufferSource;
    }

    public @Nullable RenderTarget exampleTarget() {
        return this.exampleTarget;
    }

    public void initExampleTarget(RenderTarget exampleTarget) {
        if (this.exampleTarget == null)
            this.exampleTarget = exampleTarget;
    }
}
