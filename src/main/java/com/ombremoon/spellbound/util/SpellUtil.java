package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.Spellbound;
import com.ombremoon.spellbound.common.capability.SpellHandler;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;

public class SpellUtil {

    @SuppressWarnings("unchecked")
    public static SpellHandler getSpellHandler(LivingEntity livingEntity) {
        return (SpellHandler) livingEntity.getCapability(Spellbound.ENTITY);
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

    @SuppressWarnings("unchecked")
    public static ObjectOpenHashSet<AbstractSpell> getActiveSpells(LivingEntity livingEntity) {
        return getSpellHandler(livingEntity).getActiveSpells();
    }

    public static void activateSpell(LivingEntity livingEntity, AbstractSpell abstractSpell) {
        getActiveSpells(livingEntity).add(abstractSpell);
        setRecentlyActivatedSpell(livingEntity, abstractSpell);
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashSet<SpellType<?>> getSpellSet(LivingEntity livingEntity) {
        return getSpellHandler(livingEntity).getSpellSet();
    }

    public static AbstractSpell getRecentlyActivatedSpell(LivingEntity livingEntity) {
        return getSpellHandler(livingEntity).getRecentlyActivatedSpell();
    }

    public static void setRecentlyActivatedSpell(LivingEntity livingEntity, AbstractSpell abstractSpell) {
        getSpellHandler(livingEntity).setRecentlyActivatedSpell(abstractSpell);
    }

    public static SpellType<?> getSelectedSpell(LivingEntity livingEntity) {
        return getSpellHandler(livingEntity).getSelectedSpell();
    }

    @SuppressWarnings("unchecked")
    public static void setSelectedSpell(LivingEntity livingEntity, SpellType<?> spellType) {
        getSpellHandler(livingEntity).setSelectedSpell(spellType);
        if (livingEntity instanceof ServerPlayer player) {
//            ModNetworking.syncCap(player);
        }
    }

    public static boolean isChannelling(Player player) {
        return getSpellHandler(player).isChannelling();
    }

    public static void setChannelling(ServerPlayer player, boolean channelling) {
        getSpellHandler(player).setChannelling(channelling);
//        ModNetworking.syncCap(player);
    }
}
