package com.ombremoon.spellbound.client.renderer.spell;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class EmissiveSpellRenderer<T extends SpellEntity> extends GenericSpellRenderer<T> {
    public EmissiveSpellRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
