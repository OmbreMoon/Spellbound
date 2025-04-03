package com.ombremoon.spellbound.client.event.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.client.renderer.layer.SpellLayerRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public abstract class SBRenderEvent {

    public abstract static class SpellLayer extends Event implements GeoRenderEvent {
        private final SpellLayerRenderer<?> renderer;

        public SpellLayer(SpellLayerRenderer<?> renderer) {
            this.renderer = renderer;
        }

        @Override
        public SpellLayerRenderer<?> getRenderer() {
            return this.renderer;
        }

        public net.minecraft.world.entity.LivingEntity getCaster() {
            return getRenderer().getCurrentEntity();
        }

        public static class Pre extends SpellLayer implements ICancellableEvent {
            private final PoseStack poseStack;
            private final BakedGeoModel model;
            private final MultiBufferSource bufferSource;
            private final float partialTick;
            private final int packedLight;

            public Pre(SpellLayerRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
                super(renderer);
                this.poseStack = poseStack;
                this.model = model;
                this.bufferSource = bufferSource;
                this.partialTick = partialTick;
                this.packedLight = packedLight;
            }

            public PoseStack getPoseStack() {
                return this.poseStack;
            }

            public BakedGeoModel getModel() {
                return this.model;
            }

            public MultiBufferSource getBufferSource() {
                return this.bufferSource;
            }

            public float getPartialTick() {
                return this.partialTick;
            }

            public int getPackedLight() {
                return this.packedLight;
            }
        }

        public static class Post extends SpellLayer {
            private final PoseStack poseStack;
            private final BakedGeoModel model;
            private final MultiBufferSource bufferSource;
            private final float partialTick;
            private final int packedLight;

            public Post(SpellLayerRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
                super(renderer);
                this.poseStack = poseStack;
                this.model = model;
                this.bufferSource = bufferSource;
                this.partialTick = partialTick;
                this.packedLight = packedLight;
            }

            public PoseStack getPoseStack() {
                return this.poseStack;
            }

            public BakedGeoModel getModel() {
                return this.model;
            }

            public MultiBufferSource getBufferSource() {
                return this.bufferSource;
            }

            public float getPartialTick() {
                return this.partialTick;
            }

            public int getPackedLight() {
                return this.packedLight;
            }
        }

        public static class CompileRenderLayers extends SpellLayer {
            public CompileRenderLayers(SpellLayerRenderer<?> renderer) {
                super(renderer);
            }

            public void addLayer(GeoRenderLayer renderLayer) {
                getRenderer().addRenderLayer(renderLayer);
            }
        }
    }
}
