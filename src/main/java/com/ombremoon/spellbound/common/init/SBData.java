package com.ombremoon.spellbound.common.init;

import com.mojang.serialization.Codec;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.EffectManager;
import com.ombremoon.spellbound.common.magic.tree.UpgradeTree;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

public class SBData {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID
    );
    public static final DeferredRegister.DataComponents COMPONENT_TYPES = DeferredRegister.createDataComponents(
            Registries.DATA_COMPONENT_TYPE, Constants.MOD_ID
    );

    //Handlers
    public static final Supplier<AttachmentType<SpellHandler>> SPELL_HANDLER = ATTACHMENT_TYPES.register(
            "spell_handler", () -> AttachmentType.serializable(SpellHandler::new).copyOnDeath().build());
    public static final Supplier<AttachmentType<SkillHolder>> SKILL_HOLDER = ATTACHMENT_TYPES.register(
            "skill_handler", () -> AttachmentType.serializable(SkillHolder::new).copyOnDeath().build());
    public static final Supplier<AttachmentType<EffectManager>> STATUS_EFFECTS = ATTACHMENT_TYPES.register(
            "effect_handler", () -> AttachmentType.serializable(EffectManager::new).build());

    //Mana
    public static final Supplier<AttachmentType<Double>> MANA = ATTACHMENT_TYPES.register(
            "mana", () -> AttachmentType.builder(() -> 100.0).serialize(Codec.DOUBLE).build());

    //Upgrade Tree
    public static final Supplier<AttachmentType<UpgradeTree>> UPGRADE_TREE = ATTACHMENT_TYPES.register(
            "upgrade_tree", () -> AttachmentType.serializable(UpgradeTree::new).copyOnDeath().build());

    //Summons
    public static final Supplier<AttachmentType<Integer>> OWNER_ID = ATTACHMENT_TYPES.register(
            "owner_id", () -> AttachmentType.builder(() -> 0).build());
    public static final Supplier<AttachmentType<Integer>> TARGET_ID = ATTACHMENT_TYPES.register(
            "target_id", () -> AttachmentType.builder(() -> 0).build());

    //Spell Data
    public static final Supplier<AttachmentType<Integer>> SPELL_ID = ATTACHMENT_TYPES.register(
            "spell_id", () -> AttachmentType.builder(() -> 0).build());
    public static final Supplier<AttachmentType<Integer>> FEAR_TICK = ATTACHMENT_TYPES.register(
            "fear_tick", () -> AttachmentType.builder(() -> 0).build());
    public static final Supplier<AttachmentType<Vec3>> FEAR_SOURCE = ATTACHMENT_TYPES.register(
            "fear_source", () -> AttachmentType.builder(() -> Vec3.ZERO).build());
    public static final Supplier<AttachmentType<Integer>> HEAT_TICK = ATTACHMENT_TYPES.register(
            "heat_tick", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
    public static final Supplier<AttachmentType<Boolean>> FORCE_WARP = ATTACHMENT_TYPES.register(
            "force_warp", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
    public static final Supplier<AttachmentType<Integer>> CATCH_TICK = ATTACHMENT_TYPES.register(
            "catch_tick", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
    public static final Supplier<AttachmentType<Integer>> THROWN_TICK = ATTACHMENT_TYPES.register(
            "thrown_tick", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
    public static final Supplier<AttachmentType<Integer>> STORMSTRIKE_OWNER = ATTACHMENT_TYPES.register(
            "stormstrike_owner", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
    public static final Supplier<AttachmentType<Boolean>> STORMSTRIKE_FLAG = ATTACHMENT_TYPES.register(
            "stormstrike_flag", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());

    //Misc
    public static final Supplier<AttachmentType<Integer>> RUNE_COLOR = ATTACHMENT_TYPES.register(
            "rune_color", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

    //Components
    public static final Supplier<DataComponentType<String>> SPELL = COMPONENT_TYPES.registerComponentType("spells",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final Supplier<DataComponentType<Integer>> RUNE_INDEX = COMPONENT_TYPES.registerComponentType("rune_type",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final Supplier<DataComponentType<List<Integer>>> RUNES = COMPONENT_TYPES.registerComponentType("runes",
            builder -> builder.persistent(Codec.INT.listOf()).networkSynchronized(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())));

    public static void register(IEventBus modEventBus) {
        SBData.ATTACHMENT_TYPES.register(modEventBus);
        SBData.COMPONENT_TYPES.register(modEventBus);
    }
}
