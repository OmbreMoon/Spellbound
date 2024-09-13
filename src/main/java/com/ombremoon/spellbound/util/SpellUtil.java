package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Iterator;

public class SpellUtil {

    public static SpellHandler getSpellHandler(LivingEntity livingEntity) {
        return livingEntity.getData(DataInit.SPELL_HANDLER);
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
        handler.consumeMana(abstractSpell.getManaCost(), true);
    }

    public static boolean canCastSpell(Player player, AbstractSpell spell) {
        return getSpellHandler(player).consumeMana(spell.getManaCost(), false);
    }

    public static <T extends SpellType<?>> void cycle(SpellHandler handler, T activeSpell) {
        var spellType = findNextSpellInList(handler.getSpellList(), activeSpell);
        if (spellType != activeSpell) {
            handler.setSelectedSpell(spellType);
        }
    }

    private static  <T> T findNextSpellInList(Collection<T> spellList, T currentSpell) {
        Iterator<T> iterator = spellList.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().equals(currentSpell)) {
                if (iterator.hasNext()) {
                    return iterator.next();
                }
                return spellList.iterator().next();
            }
        }
        return iterator.next();
    }
}
