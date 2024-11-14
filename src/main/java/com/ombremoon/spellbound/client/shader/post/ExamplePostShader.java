package com.ombremoon.spellbound.client.shader.post;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.CommonClass;

public class ExamplePostShader extends PostShader {
    public ExamplePostShader() {
        super(CommonClass.customLocation("shaders/post/example.json"));
    }

    @Override
    void prepShader(PoseStack poseStack) {

    }

    @Override
    void cleanupShader() {

    }
}
