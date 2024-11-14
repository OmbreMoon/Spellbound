package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.ModifierSkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Used to modify certain attributes (specifically mana, duration, or potency) of a spell or category of spells. Spell modifiers must be registered in the *insert event here* and used as parameters for {@link ModifierSkill}s.
 * @see AbstractSpell#addTimedModifier(LivingEntity, SpellModifier, int)
 * @param id The resource location of the spell modifier
 * @param modifierType The type of attribute that modifier affects
 * @param spellPredicate The condition necessary for the modifier to take effect
 * @param modifier The amount the attribute is modified by. Modifiers are <b><u>ALWAYS</u></b> multiplicative.
 */
public record SpellModifier(ResourceLocation id, ModifierType modifierType, Predicate<SpellType<?>> spellPredicate, float modifier) {
    private static final Map<ResourceLocation, SpellModifier> MODIFIER_REGISTRY = new HashMap<>();

    public static final SpellModifier UNWANTED_GUESTS = registerModifier("unwanted_guests", ModifierType.POTENCY, spellType -> true, 0.9F);
    public static final SpellModifier REPRISAL = registerModifier("reprisal", ModifierType.POTENCY, spellType -> spellType.getPath() == SpellPath.DIVINE, 1.5F);
    public static final SpellModifier ICE_CHARGE = registerModifier("ice_charge", ModifierType.MANA, spell -> spell.getPath() == SpellPath.RUIN && spell.getSubPath() == SpellPath.FROST, 1.25F);
    public static final SpellModifier CHARGED_ATMOSPHERE = registerModifier("charged_atmosphere", ModifierType.MANA, spell -> spell.getPath() == SpellPath.RUIN && spell.getSubPath() == SpellPath.SHOCK, 0.75F);
    public static final SpellModifier SUPERCHARGE = registerModifier("supercharge", ModifierType.POTENCY, spell -> spell.getPath() == SpellPath.RUIN && spell.getSubPath() == SpellPath.SHOCK, 1.5F);
    public static final SpellModifier DIVINE_BALANCE_MANA = registerModifier("divine_balance_mana", ModifierType.MANA, spell -> spell == SBSpells.HEALING_TOUCH.get(), 1.5F);
    public static final SpellModifier DIVINE_BALANCE_DURATION = registerModifier("divine_balance_duration", ModifierType.DURATION, spell -> spell == SBSpells.HEALING_TOUCH.get(), 2F);
    public static final SpellModifier SYNTHESIS = registerModifier("synthesis", ModifierType.MANA, spell -> spell == SBSpells.WILD_MUSHROOM.get(), 0F);
    public static final SpellModifier EVERLASTING_BOND = registerModifier("everlasting_bond", ModifierType.DURATION, spell -> spell == SBSpells.SHADOWBOND.get(), 2F);
    public static final SpellModifier ENDURANCE = registerModifier("endurance", ModifierType.DURATION, spell -> spell == SBSpells.THUNDEROUS_HOOVES.get(), 2F);
    public static final SpellModifier FORESIGHT = registerModifier("foresight", ModifierType.MANA, spell -> spell == SBSpells.MYSTIC_ARMOR.get(), 0.85F);
    public static final SpellModifier GALE_FORCE = registerModifier("gale_force", ModifierType.DURATION, spell -> spell == SBSpells.CYCLONE.get(), 2F);
    public static final SpellModifier TEMP = registerModifier("temp", ModifierType.CAST_CHANCE, spell -> true, 0F);

    private static SpellModifier registerModifier(String name, ModifierType type, Predicate<SpellType<?>> spellPredicate, float modifier) {
        SpellModifier spellModifier = new SpellModifier(CommonClass.customLocation(name), type, spellPredicate, modifier);
        registerModifier(spellModifier);
        return spellModifier;
    }

    public static void registerModifier(SpellModifier modifier) {
        if (MODIFIER_REGISTRY.containsValue(modifier)) throw new IllegalStateException("Modifier " + modifier + " has already been registered");
        MODIFIER_REGISTRY.putIfAbsent(modifier.id(), modifier);
    }

    public static SpellModifier getTypeFromLocation(ResourceLocation resourceLocation) {
        return MODIFIER_REGISTRY.getOrDefault(resourceLocation, null);
    }

    @Override
    public String toString() {
        return id().toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof SpellModifier spellModifier && id().equals(spellModifier.id());
        }
    }
}
