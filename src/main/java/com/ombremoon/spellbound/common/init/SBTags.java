package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.Block;

public class SBTags {
    public static class DamageTypes {
        public static final TagKey<DamageType> SPELL_DAMAGE = tag("spell_damage");
        public static final TagKey<DamageType> PHYSICAL_DAMAGE = tag("physical_damage");

        private static TagKey<DamageType> tag(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, CommonClass.customLocation(name));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> ARCANTHUS_GROWTH_BLOCKS = tag("arcanthus_growth_blocks");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, CommonClass.customLocation(name));
        }
    }
}
