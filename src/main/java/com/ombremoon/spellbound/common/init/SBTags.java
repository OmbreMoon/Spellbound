package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class SBTags {
    public static class DamageTypes {
        public static final TagKey<DamageType> SPELL_DAMAGE = tag("spell_damage");
        public static final TagKey<DamageType> PHYSICAL_DAMAGE = tag("physical_damage");

        private static TagKey<DamageType> tag(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, CommonClass.customLocation(name));
        }
    }
}