package com.ombremoon.spellbound.client.model.entity;

import com.ombremoon.spellbound.main.CommonClass;
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
        return CommonClass.customLocation("geo/entity/solar_ray.geo.json");
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
        Entity owner = animatable.getOwner();
        if (owner instanceof LivingEntity living) {
            var skills = SpellUtil.getSkills(living);
            if (skills.hasSkill(SBSkills.SUNSHINE.value()))
                name = name + "_extended";
        }
        return name;
    }
}
