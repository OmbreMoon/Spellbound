package com.ombremoon.spellbound.client.shader;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.client.shader.post.HeatDistortionPostShader;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.client.shader.post.ExamplePostShader;
import com.ombremoon.spellbound.client.shader.post.PostShader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SBShaders {
    private static final List<PostShader> POST_SHADERS = new ArrayList<>();

    //Post-Processing
    public static final ExamplePostShader EXAMPLE_SHADER = new ExamplePostShader();
    public static final HeatDistortionPostShader HEAT_DISTORTION_SHADER = new HeatDistortionPostShader();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        registerPostShader(EXAMPLE_SHADER);
        registerPostShader(HEAT_DISTORTION_SHADER);

        event.enqueueWork(() -> POST_SHADERS.forEach(PostShader::initShader));
//        POST_SHADERS.forEach(PostShader::initShader);
    }

    public static void registerPostShader(PostShader postShader) {
        POST_SHADERS.add(postShader);
    }

    public static void resize(int width, int height) {
        POST_SHADERS.forEach(postShader -> postShader.resize(width, height));
    }

    public static void processShaders() {
        POST_SHADERS.forEach(PostShader::processShader);
    }

    public static void blitShaderTargets() {
        POST_SHADERS.forEach(postShader -> {
//            if (postShader.getShaderTarget() != null)
        });
    }

    public static void setupPoseStack(PoseStack poseStack) {
        POST_SHADERS.forEach(postShader -> postShader.setupShaderStack(poseStack));
    }
}
