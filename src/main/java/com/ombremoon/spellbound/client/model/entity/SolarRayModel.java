package com.ombremoon.spellbound.client.model.entity;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.model.GeoModel;

public class SolarRayModel extends GeoModel<SolarRay> {

    @Override
    public ResourceLocation getModelResource(SolarRay animatable) {
        return CommonClass.customLocation("geo/entity/" + getName(animatable) + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SolarRay animatable) {
        return CommonClass.customLocation("textures/entity/solar_ray.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SolarRay animatable) {
        return CommonClass.customLocation("animations/entity/" + getName(animatable) + ".animation.json");
    }

    protected String getName(SolarRay animatable) {
        String name = "solar_ray";
        boolean isExtended = false;
        Entity owner = animatable.getOwner();
        if (owner instanceof LivingEntity living) {
            var skills = SpellUtil.getSkillHolder(living);
            if (skills.hasSkill(SBSkills.SUNSHINE.value()))
                isExtended = true;
        }
        return isExtended ? name + "_extended" : name;
    }
}