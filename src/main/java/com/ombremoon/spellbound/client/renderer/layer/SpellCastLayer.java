package com.ombremoon.spellbound.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.util.ClientUtil;

public class SpellCastLayer<T extends AbstractSpell> extends SpellCastRenderer<T> {
    public SpellCastLayer() {
        super(new SpellCastModel<>());
    }

    @Override
    public void renderFinal(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {
        var optional = model.getBone("bipedRightArm");
//        Constants.LOG.info("{}", optional.get().getWorldPosition());
        if (optional.isPresent() && this.currentEntity.tickCount % 5 == 0) {
            Vector3d vec3 = optional.get().getWorldPosition();
//            Constants.LOG.info("{}", vec3);
//            ClientUtil.getLevel().addParticle(
//                    ParticleTypes.SNOWFLAKE,
//                    vec3.x,
//                    vec3.y,
//                    vec3.z,
//                    0,
//                    0,
//                    0
//            );
        }
    }
}
