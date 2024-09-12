package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.Spellbound;
import com.ombremoon.spellbound.common.capability.ISpellHandler;
import com.ombremoon.spellbound.common.capability.SpellHandler;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class SpellUtil {

    public static SpellHandler getSpellHandler(LivingEntity livingEntity) {
        return (SpellHandler) livingEntity.getCapability(Spellbound.ENTITY);
    }

    public static ISpellHandler getISpellHandler(LivingEntity livingEntity) {
        return livingEntity.getCapability(Spellbound.ENTITY);
    }

    public static CompoundTag storeSpell(SpellType<?> spellType) {
        CompoundTag compoundTag = new CompoundTag();
        return storeSpell(compoundTag, spellType);
    }

    private static CompoundTag storeSpell(CompoundTag compoundTag, SpellType<?> spellType) {
        compoundTag.putString("Spell", spellType.getResourceLocation().toString());
        return compoundTag;
    }

    public static ResourceLocation getSpellId(CompoundTag compoundTag, String tagKey) {
        return ResourceLocation.tryParse(compoundTag.getString(tagKey));
    }

    public static void activateSpell(LivingEntity livingEntity, AbstractSpell abstractSpell) {
        var handler = getSpellHandler(livingEntity);
        handler.getActiveSpells().add(abstractSpell);
        handler.setRecentlyActivatedSpell(abstractSpell);
    }
}
