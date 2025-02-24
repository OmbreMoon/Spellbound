package com.ombremoon.spellbound.datagen;

import com.google.common.collect.ImmutableMap;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.common.init.SBItems;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ModLangProvider extends LanguageProvider {

    protected static final Map<String, String> REPLACE_LIST = ImmutableMap.of(
            "tnt", "TNT",
            "sus", ""
    );

    public ModLangProvider(PackOutput gen) {
        super(gen, Constants.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        SBItems.ITEMS.getEntries().forEach(this::itemLang);
        SBSpells.SPELL_TYPES.getEntries().forEach(this::spellLang);
        SBSkills.SKILLS.getEntries().forEach(this::skillLang);
//        BlockInit.BLOCKS.getEntries().forEach(this::blockLang);
//        EntityInit.ENTITIES.getEntries().forEach(this::entityLang);
//        StatusEffectInit.STATUS_EFFECTS.getEntries().forEach(this::effectLang);

        manualEntries();
    }

    protected void itemLang(DeferredHolder<Item, ? extends Item> entry) {
        if (!(entry.get() instanceof BlockItem) || entry.get() instanceof ItemNameBlockItem) {
            addItem(entry, checkReplace(entry));
        }
    }

    protected void spellLang(DeferredHolder<SpellType<?>, ? extends SpellType<?>> entry) {
        add(entry.get().createSpell().getNameId(), checkReplace(entry));
    }

    protected void skillLang(DeferredHolder<Skill, ? extends Skill> entry) {
        add(entry.get().getNameId(), checkReplace(entry));
    }

    protected void blockLang(DeferredHolder<Block, ? extends Block> entry) {
        addBlock(entry, checkReplace(entry));
    }

    protected void entityLang(DeferredHolder<EntityType<?>, ? extends EntityType<?>> entry) {
        addEntityType(entry, checkReplace(entry));
    }

    protected void effectLang(DeferredHolder<MobEffect, ? extends MobEffect> entry) {
        addEffect(entry, checkReplace(entry));
    }

    protected void manualEntries() {
        skillDescriptions();
        add("chat.spelltome.awardxp", "Spell already known. +10 spells XP.");
        add("chat.spelltome.nospell", "This spells tome is blank.");
        add("chat.spelltome.spellunlocked", "Spell unlocked: %1$s");
        add("tooltip.spellbound.holdshift", "Hold shift for more information.");

        add("command.spellbound.spellunknown", "You don't know the spells %1$s.");
        add("command.spellbound.spellforgot", "%1$s has been forgotten successfully.");
        add("command.spellbound.alreadyknown", "%1$s is already known.");
        add("command.spellbound.singleskilllearnt", "%1$s has been unlocked.");
        add("command.spellbound.learntskills", "All skills unlocked for %1$s");
        add("command.spellbound.spelllearnt", "%1%s has been learnt.");
    }

    protected void skillDescriptions() {
        addSkillTooltip(SBSkills.VOLCANO, "Create a volcanic eruption that spits out 8 lava bombs per second for 10 seconds.");
        addSkillTooltip(SBSkills.INFERNO_CORE, "After the eruption ends, the volcano drops a Smoldering Shard.");
        addSkillTooltip(SBSkills.EXPLOSIVE_BARRAGE, "Each lava bomb explodes on impact.");
        addSkillTooltip(SBSkills.LAVA_FLOW, "Lava bombs turn into lava pools on impact.");

        addSkillTooltip(SBSkills.STORMSTRIKE, "Send out a bolt of lightning that charges a target, dealing 2 shock damage per second for 3 seconds.");
        addSkillTooltip(SBSkills.STATIC_SHOCK, "Hitting a block now creates a small explosion that applies Stormstrike to anyone it hits.");
        addSkillTooltip(SBSkills.ELECTRIFY, "Decrease the target's shock resistance by 30%.");
        addSkillTooltip(SBSkills.SHOCK_FACTOR, "Deals extra damage equal to 1% of your current mana each damage tick.");
        addSkillTooltip(SBSkills.PURGE, "Deals extra damage to summoned targets, equal to 10% of the caster's current mana.");
        addSkillTooltip(SBSkills.REFRACTION, "When the target takes damage from your shock-based Ruin spells while affected with Stormstrike, restores 15 mana back to the caster.");
        addSkillTooltip(SBSkills.PULSATION, "Chance to paralyze the target for 1 second each damage tick.");
        addSkillTooltip(SBSkills.STORM_SHARD, "If the target dies while affected by Stormstrike, the caster is awarded a Storm Shard.");
        addSkillTooltip(SBSkills.CHARGED_ATMOSPHERE, "Decreases shock-based Ruin spells' mana costs by 25% for 8 seconds.");
        addSkillTooltip(SBSkills.DISCHARGE, "Chance to disarm the target each damage tick.");
        addSkillTooltip(SBSkills.SUPERCHARGE, "If the target dies while affected by Stormstrike, increases the damage of shock-based Ruin spells by 50% for 10 seconds.");

        addSkillTooltip(SBSkills.STORM_RIFT, "Creates a storm portal for 20 seconds. If two portals are active, those approaching either get warped across and take 5 shock damage to health and mana.");
        addSkillTooltip(SBSkills.STORM_FURY, "The vortex doubles in size and damage.");
        addSkillTooltip(SBSkills.DISPLACEMENT_FIELD, "A single portal can now teleport enemies to a random location within 10 blocks.");
        addSkillTooltip(SBSkills.MAGNETIC_FIELD, "The vortex has twice the pull strength. Enemies caught in the field have their armor reduced by 25%.");
        addSkillTooltip(SBSkills.EVENT_HORIZON, "When a target is warped, they pull nearby enemies towards the warp field.");
        addSkillTooltip(SBSkills.CHARGED_RIFT, "Each warp between portals charges the storm, increasing shock damage 1 up to a max of 5.");
        addSkillTooltip(SBSkills.MOTION_SICKNESS, "Warped enemies have their movement, attack, and mining speed reduced by 40% for 10 seconds.");
        addSkillTooltip(SBSkills.FORCED_WARP, "Upon being warped, targets are launched out of the portal with a high velocity, potentially dealing damage on impact.");
        addSkillTooltip(SBSkills.STORM_CALLER, "Generate a cloud above both portals that discharge lightning periodically dealing for 5 shock damage.");
        addSkillTooltip(SBSkills.IMPLOSION, "Recast while targeting a portal with a storm shard to detonate the portal, applying Stormstrike to anyone in the area.");
        addSkillTooltip(SBSkills.ORBITAL_SHELL, "Recast while targeting a portal with a shard to mark a portal. Marked portals will move in a 3-block radius circle centered around the origin.");

        addSkillTooltip(SBSkills.STRIDE, "Movement speed is increased by 25% for 30 seconds.");
        addSkillTooltip(SBSkills.QUICK_SPRINT, "For the first 10 seconds, movement speed is increased by an additional 15%.");
        addSkillTooltip(SBSkills.GALLOPING_STRIDE, "Speed is increased by another 25%.");
        addSkillTooltip(SBSkills.RIDERS_RESILIENCE, "All movement benefits are applied to mounts.");
        addSkillTooltip(SBSkills.FLEETFOOTED, "Nearby allies gain 15% movement speed while near the caster.");
        addSkillTooltip(SBSkills.SUREFOOTED, "Step height is increased.");
        addSkillTooltip(SBSkills.AQUA_TREAD, "Gain the ability to walk on water.");
        addSkillTooltip(SBSkills.ENDURANCE, "Duration is increased by 30 seconds.");
        addSkillTooltip(SBSkills.MOMENTUM, "For each second travelled, your attack speed is increased by 4%, up to a max of 20%, for 5 seconds.");
        addSkillTooltip(SBSkills.STAMPEDE, "You can charge through enemies, knocking them back and dealing 3 damage.");
        addSkillTooltip(SBSkills.MARATHON, "Food consumption is halted.");

        addSkillTooltip(SBSkills.SHADOW_GATE, "Deploy 2 shadow portals (must be in a low light level), allowing passage in both directions with 50 blocks.");
        addSkillTooltip(SBSkills.REACH, "Double the range of the portals");
        addSkillTooltip(SBSkills.BLINK, "Passing through the portals increases the caster's movement speed for 25 seconds.");
        addSkillTooltip(SBSkills.SHADOW_ESCAPE, "When the caster enters the portal below 50% health, they gain invisibility for 10 seconds after exiting.");
        addSkillTooltip(SBSkills.OPEN_INVITATION, "Anyone can pass through the portals.");
        addSkillTooltip(SBSkills.QUICK_RECHARGE, "The caster receives 20 mana any time someone passes through a portal.");
        addSkillTooltip(SBSkills.UNWANTED_GUESTS, "Enemies that pass through a portal have their attack and spell damage reduced by 10%.");
        addSkillTooltip(SBSkills.BAIT_AND_SWITCH, "Enemies passing through a portal take 5 damage to health and mana.");
        addSkillTooltip(SBSkills.DARKNESS_PREVAILS, "Portals can be spawned in any light level.");
        addSkillTooltip(SBSkills.GRAVITY_SHIFT, "Exiting the portal launches entities in the air, applying slow falling to the caster and allies, if applicable.");
        addSkillTooltip(SBSkills.DUAL_DESTINATION, "Can now deploy an additional portal. Order of travel goes by order placed.");

        addSkillTooltip(SBSkills.HEALING_BLOSSOM, "Plants a divine blossom. The blossom blooms 10 seconds after casting and last 10 seconds. The blossom heals the caster 2 health per seconds when within 5 blocks.");
        addSkillTooltip(SBSkills.THORNY_VINES, "Enemies within the range of the blossom take 4 damage per second.");
        addSkillTooltip(SBSkills.BLOOM, "The blossom now activates immediately after casting.");
        addSkillTooltip(SBSkills.ETERNAL_SPRING, "The healing duration is increased to 15 seconds.");
        addSkillTooltip(SBSkills.FLOWER_FIELD, "Allies receive half the healing from the blossom.");
        addSkillTooltip(SBSkills.FLOURISHING_GROWTH, "If the caster's health reaches full, the excess health is converted into 5 points of mana per second.");
        addSkillTooltip(SBSkills.HEALING_WINDS, "The blossom now follows the caster.");
        addSkillTooltip(SBSkills.BURST_OF_LIFE, "Instantly heals the caster 4 health upon activation.");
        addSkillTooltip(SBSkills.PETAL_SHIELD, "The caster gains 20% damage resistance.");
        addSkillTooltip(SBSkills.VERDANT_RENEWAL, "Cleanses all negative effects from the caster");
        addSkillTooltip(SBSkills.REBIRTH, "Mark a blossom with a holy shard. If the caster takes fatal damage near the blossom, half of their health is automatically restored.");

        addSkillTooltip(SBSkills.SHADOWBOND, "Caster and target gain invisibility for 10 seconds. When the invisibility is broken,the caster and target swap places.");
        addSkillTooltip(SBSkills.EVERLASTING_BOND, "Increases the duration of invisibility to 20 seconds.");
        addSkillTooltip(SBSkills.SHADOW_STEP, "After the swap, the caster's movement speed is increased by 30% for 5 seconds");
        addSkillTooltip(SBSkills.SNEAK_ATTACK, "After the swap, the caster's first attack within 5 seconds deals 50% more damage.");
        addSkillTooltip(SBSkills.SILENT_EXCHANGE, "After the swap, the target is Silenced for 5 seconds.");
        addSkillTooltip(SBSkills.SNARE, "After the swap, the target is rooted.");
        addSkillTooltip(SBSkills.DISORIENTED, "After the swap, the target gets dizzy and deals 20% less damage for 5 seconds.");
        addSkillTooltip(SBSkills.OBSERVANT, "The target is outlined to the caster while invisible.");
        addSkillTooltip(SBSkills.REVERSAL, "After the swap, the caster can recast with a fool's shard within 5 seconds to swap back with the target.");
        addSkillTooltip(SBSkills.LIVING_SHADOW, "After the swap, the caster remains invisible for another 5 seconds and leaves behind a decoy for 10 seconds.");
        addSkillTooltip(SBSkills.SHADOW_CHAIN, "The spell can now affect an additional target. Swapping order is in the order of targets affected.");
    }

    protected void addSkillTooltip(Holder<Skill> skill, String description) {
        add(skill.value().getDescriptionId(), description);
    }

    protected String checkReplace(DeferredHolder<?, ?> holder) {
        return Arrays.stream(holder.getId().getPath().split("_"))
                .map(this::checkReplace)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }

    protected String checkReplace(String string) {
        return REPLACE_LIST.containsKey(string) ? REPLACE_LIST.get(string) : StringUtils.capitalize(string);
    }

}
