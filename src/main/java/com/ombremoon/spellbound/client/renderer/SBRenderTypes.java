package com.ombremoon.spellbound.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.ombremoon.spellbound.Spellbound;
import com.ombremoon.spellbound.client.ClientStuff;
import com.ombremoon.spellbound.client.shader.SBShaders;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;

public class SBRenderTypes {
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_BLOOM_SHADER = new RenderStateShard.ShaderStateShard(SBShaders::getBloomShader);
    public static final RenderStateShard.OutputStateShard EXAMPLE_TARGET = new RenderStateShard.OutputStateShard(
            "example_target",
            () -> ClientStuff.getInstance().getExamples().exampleTarget().bindWrite(false),
            () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false)
    );

    public static RenderType bloom(ResourceLocation location) {
        return BLOOM.apply(location, RenderStateShard.ADDITIVE_TRANSPARENCY);
    }

    public static RenderType example(ResourceLocation location) {
        return EXAMPLE.apply(location, RenderStateShard.NO_CULL);
    }

    public static final BiFunction<ResourceLocation, RenderStateShard.CullStateShard, RenderType> EXAMPLE = Util.memoize(
            (location, cullStateShard) -> RenderType.create(
                    "example",
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS,
                    1536,
                    RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_BLOOM_SHADER)
                            .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                            .setCullState(cullStateShard)
                            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                            .setOutputState(EXAMPLE_TARGET)
                            .createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)
            )
    );

    static final BiFunction<ResourceLocation, RenderStateShard.TransparencyStateShard, RenderType> BLOOM = Util.memoize(
            (location, transparencyStateShard) -> RenderType.create(
                    "bloom",
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS,
                    1536,
                    RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_BLOOM_SHADER)
                            .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                            .setTransparencyState(transparencyStateShard)
                            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                            .setOutputState(EXAMPLE_TARGET)
                            .createCompositeState(false)
            )
    );
}
