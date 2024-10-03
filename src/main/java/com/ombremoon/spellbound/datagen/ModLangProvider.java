package com.ombremoon.spellbound.datagen;

import com.google.common.collect.ImmutableMap;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.ItemInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
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
        ItemInit.ITEMS.getEntries().forEach(this::itemLang);
        SpellInit.SPELL_TYPES.getEntries().forEach(this::spellLang);
        SkillInit.SKILLS.getEntries().forEach(this::skillLang);
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
        add("chat.spelltome.awardxp", "Spell already known. +10 spell XP.");
        add("chat.spelltome.nospell", "This spell tome is blank.");
        add("chat.spelltome.spellunlocked", "Spell unlocked: %1$s");
        add("tooltip.spellbound.holdshift", "Hold shift for more information.");

        add("command.spellbound.spellunknown", "You don't know the spell %1$s.");
        add("command.spellbound.spellforgot", "%1$s has been forgotten successfully.");
        add("command.spellbound.alreadyknown", "%1$s is already known.");
        add("command.spellbound.singleskilllearnt", "%1$s has been unlocked.");
        add("command.spellbound.learntskills", "All skills unlocked for %1$s");
        add("command.spellbound.spelllearnt", "%1%s has been learnt.");
    }

    protected void skillDescriptions() {
        addSkillTooltip(SkillInit.VOLCANO, "Create a volcanic eruption that spits out 8 lava bombs per second for 10 seconds.");
        addSkillTooltip(SkillInit.INFERNO_CORE, "After the eruption ends, the volcano drops a Smoldering Shard.");
        addSkillTooltip(SkillInit.EXPLOSIVE_BARRAGE, "Each lava bomb explodes on impact.");
        addSkillTooltip(SkillInit.LAVA_FLOW, "Lava bombs turn into lava pools on impact.");
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
