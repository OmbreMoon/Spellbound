package com.ombremoon.spellbound.client.shader;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.ombremoon.spellbound.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;

public class ExampleBufferSource implements MultiBufferSource {
    private final MultiBufferSource.BufferSource source;
    private final MultiBufferSource.BufferSource exampleSource = MultiBufferSource.immediate(new ByteBufferBuilder(1536));
    private int colorR = 255;
    private int colorG = 255;
    private int colorB = 255;
    private int colorA = 255;

    public ExampleBufferSource(MultiBufferSource.BufferSource source) {
        this.source = source;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (renderType.isOutline()) {
            VertexConsumer consumer = this.exampleSource.getBuffer(renderType);
            return new ExampleGenerator(consumer, this.colorR, this.colorG, this.colorB, this.colorA);
        } else {
            VertexConsumer vertexconsumer = this.source.getBuffer(renderType);
            Optional<RenderType> optional = RenderUtil.getExampleRenderType(renderType);
//            Optional<RenderType> optional = renderType.outline();
            if (optional.isPresent()) {
                VertexConsumer vertexConsumer = this.exampleSource.getBuffer(optional.get());
                ExampleGenerator generator = new ExampleGenerator(vertexConsumer, this.colorR, this.colorG, this.colorB, this.colorA);
                return VertexMultiConsumer.create(generator, vertexconsumer);
            } else {
                return vertexconsumer;
            }
        }
    }

    public void endExampleBatch() {
        this.exampleSource.endBatch();
    }

    @OnlyIn(Dist.CLIENT)
    record ExampleGenerator(VertexConsumer delegate, int color) implements VertexConsumer {
        public ExampleGenerator(VertexConsumer p_109943_, int p_109944_, int p_109945_, int p_109946_, int p_109947_) {
            this(p_109943_, FastColor.ARGB32.color(p_109947_, p_109944_, p_109945_, p_109946_));
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.delegate.addVertex(x, y, z).setColor(this.color);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }
    }
}
