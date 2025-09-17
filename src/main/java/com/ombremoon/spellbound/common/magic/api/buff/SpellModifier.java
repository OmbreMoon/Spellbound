package com.ombremoon.spellbound.common.magic.api.buff;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.common.magic.skills.ModifierSkill;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Used to modify certain attributes (specifically mana, duration, potency, or cast chance) of a spell or category of spells. Spell modifiers must be registered in the *insert event here* and used as parameters for {@link ModifierSkill}s.
 * @see SkillBuff#SPELL_MODIFIER
 * @param id The resource location of the spells modifier
 * @param modifierType The type of attribute that modifier affects
 * @param spellPredicate The condition necessary for the modifier to take effect
 * @param modifier The amount the attribute is modified by. Modifiers are <b><u>ALWAYS</u></b> multiplicative.
 */
public record SpellModifier(ResourceLocation id, ModifierType modifierType, Predicate<SpellType<?>> spellPredicate, float modifier) {
    private static final Map<ResourceLocation, SpellModifier> MODIFIER_REGISTRY = new HashMap<>();
    public static final StreamCodec<ByteBuf, SpellModifier> STREAM_CODEC = ResourceLocation.STREAM_CODEC
            .map(SpellModifier::getTypeFromLocation, SpellModifier::id);

    public static final SpellModifier FEAR = registerModifier("fear", ModifierType.POTENCY, spellType -> true, 0.75F);
    public static final SpellModifier UNWANTED_GUESTS = registerModifier("unwanted_guests", ModifierType.POTENCY, spellType -> true, 0.9F);
    public static final SpellModifier REPRISAL = registerModifier("reprisal", ModifierType.POTENCY, spellType -> spellType.getPath() == SpellPath.DIVINE, 1.5F);
    public static final SpellModifier ICE_CHARGE = registerModifier("ice_charge", ModifierType.MANA, spell -> spell.getPath() == SpellPath.RUIN && spell.getSubPath() == SpellPath.FROST, 1.25F);
    public static final SpellModifier CHARGED_ATMOSPHERE = registerModifier("charged_atmosphere", ModifierType.MANA, spell -> spell.getPath() == SpellPath.RUIN && spell.getSubPath() == SpellPath.SHOCK, 0.75F);
    public static final SpellModifier SUPERCHARGE = registerModifier("supercharge", ModifierType.POTENCY, spell -> spell.getPath() == SpellPath.RUIN && spell.getSubPath() == SpellPath.SHOCK, 1.5F);
    public static final SpellModifier DIVINE_BALANCE_MANA = registerModifier("divine_balance_mana", ModifierType.MANA, spell -> spell == SBSpells.HEALING_TOUCH.get(), 1.5F);
    public static final SpellModifier DIVINE_BALANCE_DURATION = registerModifier("divine_balance_duration", ModifierType.DURATION, spell -> spell == SBSpells.HEALING_TOUCH.get(), 2F);
    public static final SpellModifier POISON_ESSENCE = registerModifier("poison_essence", ModifierType.POTENCY, spell -> spell == SBSpells.WILD_MUSHROOM.get(), 1.25F);
    public static final SpellModifier SYNTHESIS = registerModifier("synthesis", ModifierType.MANA, spell -> spell == SBSpells.WILD_MUSHROOM.get(), 0F);
    public static final SpellModifier ENDURANCE = registerModifier("endurance", ModifierType.DURATION, spell -> spell == SBSpells.STRIDE.get(), 2F);
    public static final SpellModifier FORESIGHT = registerModifier("foresight", ModifierType.MANA, spell -> spell == SBSpells.MYSTIC_ARMOR.get(), 0.85F);
//    public static final SpellModifier GALE_FORCE = registerModifier("gale_force", ModifierType.DURATION, spell -> spell == SBSpells.CYCLONE.get(), 2F);
    public static final SpellModifier RESIDUAL_DISRUPTION = registerModifier("residual_disruption", ModifierType.CAST_CHANCE, spell -> true, 0.5F);
    public static final SpellModifier UNFOCUSED = registerModifier("unfocused", ModifierType.POTENCY, spell -> true, 0.8F);

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

    @Override
    public int hashCode() {
        int i = this.id.hashCode();
        i = 31 * i + this.modifierType.hashCode();
        return 31 * i + Float.floatToIntBits(this.modifier);
    }
}
