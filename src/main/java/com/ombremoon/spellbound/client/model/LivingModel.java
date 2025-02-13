package com.ombremoon.spellbound.client.model;

import com.ombremoon.spellbound.common.content.entity.spell.SBLivingEntity;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class LivingModel<T extends SBLivingEntity> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return CommonClass.customLocation("geo/entity/" + getName(animatable) + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return CommonClass.customLocation("textures/entity/" + getName(animatable) + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return CommonClass.customLocation("animations/entity/" + getName(animatable) + ".animation.json");
    }

    protected String getName(T animatable) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(animatable.entityType()).getPath();
    }

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
