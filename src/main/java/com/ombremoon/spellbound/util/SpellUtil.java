package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.common.content.entity.ISpellEntity;
import com.ombremoon.spellbound.common.init.SBAttributes;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.init.SBEffects;
import com.ombremoon.spellbound.common.magic.EffectManager;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiPredicate;

public class SpellUtil {
    public static final BiPredicate<Entity, LivingEntity> CAN_ATTACK_ENTITY = (entity, livingEntity) -> (!livingEntity.isAlliedTo(entity) || livingEntity.is(entity)) && !livingEntity.hasEffect(SBEffects.COUNTER_MAGIC) && !(livingEntity instanceof OwnableEntity ownable && ownable.getOwner() == (entity));
    public static final BiPredicate<Entity, LivingEntity> IS_ALLIED = (entity, livingEntity) -> livingEntity.isAlliedTo(entity) || (livingEntity instanceof OwnableEntity ownable && ownable.getOwner() == (entity));

    public static SpellHandler getSpellCaster(LivingEntity livingEntity) {
        var handler = livingEntity.getData(SBData.SPELL_HANDLER);
        if (!handler.isInitialized())
            handler.initData(livingEntity);
        
        return handler;
    }

    public static SkillHolder getSkills(LivingEntity livingEntity) {
        return livingEntity.getData(SBData.SKILL_HOLDER);
    }

    public static EffectManager getSpellEffects(LivingEntity livingEntity) {
        return livingEntity.getData(SBData.STATUS_EFFECTS);
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

    public static boolean canCastSpell(LivingEntity livingEntity, AbstractSpell spell) {
        if (livingEntity instanceof Player player && player.getAbilities().instabuild) return true;
        if (EffectManager.isSilenced(livingEntity)) return false;

        var handler = getSpellCaster(livingEntity);
        return handler.inCastMode() && handler.consumeMana(spell.getManaCost(livingEntity), false);
    }

    public static <T extends SpellType<?>> void cycle(SpellHandler handler, T activeSpell) {
        var spellType = findNextSpellInList(handler.getEquippedSpells(), activeSpell);
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

    /**
     * Sets the spells data attachment of a given entity to the given spells
     * @param entity The summon/SpellEntity to set the spells of
     * @param spell The spells
     */
    public static void setSpell(@NotNull Entity entity, @NotNull AbstractSpell spell) {
        entity.setData(SBData.SPELL_ID, spell.getId());
        if (entity instanceof ISpellEntity<?> spellEntity)
            spellEntity.setSpell(spell.spellType(), spell.getId());
    }

    public static float getCastRange(LivingEntity caster) {
        return caster.getAttribute(SBAttributes.CAST_RANGE) != null ? (float) caster.getAttributeValue(SBAttributes.CAST_RANGE) : 10.0F;
    }

    public static float getCastSpeed(LivingEntity caster) {
        return caster.getAttribute(SBAttributes.CAST_SPEED) != null ? (float) caster.getAttributeValue(SBAttributes.CAST_SPEED) : 1.0F;
    }

    /**
     * Sets the owner data attachment of a given entity to the given owner
     * @param entity The summon/SpellEntity to set the owner of
     * @param owner The owner of the summon
     */
    public static void setOwner(@NotNull Entity entity, @NotNull LivingEntity owner) {
        entity.setData(SBData.OWNER_ID, owner.getId());
        if (entity instanceof ISpellEntity spellEntity)
            spellEntity.setOwner(owner);
    }

    /**
     * Gets the owner of a given entity, assuming that it is a valid summon
     * @param entity The entity to get the owner of
     * @return The Player that owns the entity, null if unowned
     */
    @Nullable
    public static Entity getOwner(@NotNull Entity entity) {
        if (!entity.hasData(SBData.OWNER_ID)) return null;
        return entity.level().getEntity(entity.getData(SBData.OWNER_ID));
    }

    /**
     * Gets the target of the current entity
     * @param entity The entity to get the target of
     * @return the current target, null if no current target
     * @apiNote Should only be used for summons
     */
    @Nullable
    public static LivingEntity getTarget(@NotNull Entity entity) {
        if (!entity.hasData(SBData.TARGET_ID)) return null;
        return (LivingEntity) entity.level().getEntity(entity.getData(SBData.TARGET_ID));
    }

    /**
     * Sets the target for a given mob both through vanilla targeting and summon targeting systems.
     * @param summon The summon to set the target of
     * @param target The new target for the summon
     */
    public static void setTarget(@NotNull PathfinderMob summon, @NotNull LivingEntity target) {
        summon.setData(SBData.TARGET_ID, target.getId());
        summon.setTarget(target);
    }

    /**
     * Gets the ids of every currently active summon created by a given caster
     * @param caster The living entity to get the summons of
     * @return Set of the ID of every summon summoned by the given caster
     */
    @NotNull
    public static Set<Integer> getAllSummonIds(@NotNull LivingEntity caster) {
        SpellHandler handler = SpellUtil.getSpellCaster(caster);
        return handler.getSummons();
    }

    /**
     * Checks if a given entity is a summon.
     * @param summon The summon to check the owner of
     * @return true if owner, false if no owner/not the owner
     */
    public static boolean isSummon(@NotNull Entity summon) {
        return getOwner(summon) != null;
    }

    /**
     * Checks if a given entity is a summon of a given living entity.
     * @param summon The summon to check the owner of
     * @param owner The player to check if they are the owner
     * @return true if owner, false if no owner/not the owner
     */
    public static boolean isSummonOf(@NotNull Entity summon, @NotNull LivingEntity owner) {
        Entity entity = getOwner(summon);
        if (entity == null) return false;
        return entity.is(owner);
    }
}
