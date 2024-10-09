package com.ombremoon.spellbound.util;

import com.ombremoon.spellbound.common.content.entity.SpellEntity;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SummonUtil {

    /**
     * Sets the owner data attachment of a given entity to the given owner
     * @param entity The summon/SpellEntity to set the owner of
     * @param owner The owner of the summon
     */
    public static void setOwner(@NotNull Entity entity, @NotNull LivingEntity owner) {
        entity.setData(DataInit.OWNER_UUID, owner.getUUID().toString());
        if (entity instanceof SpellEntity spellEntity)
            spellEntity.setOwner(owner);
    }

    /**
     * Gets the owner of a given entity, assuming that it is a valid summon
     * @param entity The entity to get the owner of
     * @return The Player that owns the entity, null if unowned
     */
    @Nullable
    public static Player getOwner(@NotNull Entity entity) {
        if (!entity.hasData(DataInit.OWNER_UUID)) return null;
        return entity.level().getPlayerByUUID(UUID.fromString(entity.getData(DataInit.OWNER_UUID)));
    }

    /**
     * Gets the target of the current entity
     * @param entity The entity to get the target of
     * @return the current target, null if no current target
     * @apiNote Should only be used for summons
     */
    @Nullable
    public static LivingEntity getTarget(@NotNull Entity entity) {
        if (!entity.hasData(DataInit.TARGET_ID)) return null;
        return (LivingEntity) entity.level().getEntity(entity.getData(DataInit.TARGET_ID));
    }

    /**
     * Sets the target for a given mob both through vanilla targeting and summon targeting systems.
     * @param summon The summon to set the target of
     * @param target The new target for the summon
     */
    public static void setTarget(@NotNull Mob summon, @NotNull LivingEntity target) {
        summon.setData(DataInit.TARGET_ID, target.getId());
        summon.setTarget(target);
    }

    /**
     * Gets the ids of every currently active summon created by a given caster
     * @param caster The living entity to get the summons of
     * @return Set of the ID of every summon summoned by the given caster
     */
    @NotNull
    public static Set<Integer> getAllSummonIds(@NotNull LivingEntity caster) {
        SpellHandler handler = SpellUtil.getSpellHandler(caster);
        Set<Integer> summons = new HashSet<>();
        for (AbstractSpell spell : handler.getActiveSpells()) {
            if (spell instanceof SummonSpell summonSpell) summons.addAll(summonSpell.getSummons());
        }

        return summons;
    }

    /**
     * Checks if a given entity is a summon of a given living entity.
     * @param summon The summon to check the owner of
     * @param owner The player to check if they are the owner
     * @return true if owner, false if no owner/not the owner
     */
    public static boolean isSummonOf(@NotNull Entity summon, @NotNull LivingEntity owner) {
        Player player = getOwner(summon);
        if (player == null) return false;
        return player.is(owner);
    }
}
