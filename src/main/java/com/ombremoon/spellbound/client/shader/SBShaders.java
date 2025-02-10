package com.ombremoon.spellbound.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.client.shader.post.Example2PostShader;
import com.ombremoon.spellbound.client.shader.post.ExamplePostShader;
import com.ombremoon.spellbound.client.shader.post.PostShader;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SBShaders {
    private static final List<PostShader> POST_SHADERS = new ArrayList<>();

    private static ShaderInstance rendertypeBloom;

    public static final ExamplePostShader EXAMPLE_SHADER = new ExamplePostShader();
    public static final Example2PostShader EXAMPLE_SHADER_2 = new Example2PostShader();

    public static ShaderInstance getBloomShader() {
        return Objects.requireNonNull(rendertypeBloom, "Attempted to call getBloomShader before shaders have finished loading.");
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), CommonClass.customLocation("rendertype_bloom"), DefaultVertexFormat.POSITION_TEX_COLOR), shaderInstance -> {
            rendertypeBloom = shaderInstance;
        });
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        registerPostShader(EXAMPLE_SHADER);
        registerPostShader(EXAMPLE_SHADER_2);
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

    public static void setupPoseStack(PoseStack poseStack) {
        POST_SHADERS.forEach(postShader -> postShader.setupShaderStack(poseStack));
    }
}
