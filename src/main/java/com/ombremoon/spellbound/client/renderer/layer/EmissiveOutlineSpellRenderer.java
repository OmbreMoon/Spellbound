package com.ombremoon.spellbound.client.renderer.layer;

import com.ombremoon.spellbound.client.renderer.GenericSpellRenderer;
import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class EmissiveOutlineSpellRenderer<T extends SpellEntity> extends GenericSpellRenderer<T> {
    public EmissiveOutlineSpellRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public @Nullable RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.itemEntityTranslucentCull(texture);
    }
}
