package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.networking.PayloadHandler;
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

    public static SkillHandler getSkillHandler(LivingEntity livingEntity) {
        return livingEntity.getData(DataInit.SKILL_HANDLER);
    }

    public static CompoundTag storeSpell(SpellType<?> spellType) {
        CompoundTag compoundTag = new CompoundTag();
        return storeSpell(compoundTag, spellType);
    }

    private static CompoundTag storeSpell(CompoundTag compoundTag, SpellType<?> spellType) {
        compoundTag.putString("Spell", spellType.location().toString());
        return compoundTag;
    }

    public static ResourceLocation getSpellId(CompoundTag compoundTag, String tagKey) {
        return ResourceLocation.tryParse(compoundTag.getString(tagKey));
    }

    public static boolean canCastSpell(Player player, AbstractSpell spell) {
        var handler = getSpellHandler(player);
        if (player.hasEffect(EffectInit.SILENCED) || player.hasEffect(EffectInit.STUNNED)) return false;
        return handler.inCastMode() && handler.consumeMana(spell.getManaCost(player), false);
    }

    public static <T extends SpellType<?>> void cycle(SpellHandler handler, T activeSpell) {
        var spellType = findNextSpellInList(handler.getSpellList(), activeSpell);
        if (spellType != activeSpell) {
            handler.setSelectedSpell(spellType);
        }
    }

    private static <T> T findNextSpellInList(Collection<T> spellList, T currentSpell) {
        Iterator<T> iterator = spellList.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().equals(currentSpell)) {
                if (iterator.hasNext()) {
                    return iterator.next();
                }
                return spellList.iterator().next();
            }
        }

        return spellList.isEmpty() ? currentSpell : spellList.iterator().next();
    }
}
