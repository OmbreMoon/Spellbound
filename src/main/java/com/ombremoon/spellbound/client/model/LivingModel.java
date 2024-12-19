package com.ombremoon.spellbound.client.model;

import com.ombremoon.spellbound.common.content.entity.ISpellEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.data.EntityModelData;

public class LivingModel<T extends ISpellEntity> extends GenericModel<T> {

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        GeoBone head = getAnimationProcessor().getBone("Head");

        if (head != null && !((LivingEntity)animatable).isDeadOrDying()) {
            EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

            head.setRotX(data.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(data.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}