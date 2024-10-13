package com.ombremoon.spellbound.common.init;

import com.mojang.serialization.Codec;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.data.EffectHandler;
import com.ombremoon.spellbound.common.magic.tree.UpgradeTree;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class DataInit {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID
    );
    public static final DeferredRegister.DataComponents COMPONENT_TYPES = DeferredRegister.createDataComponents(
            Registries.DATA_COMPONENT_TYPE, Constants.MOD_ID
    );

    //Handlers
    public static final Supplier<AttachmentType<SpellHandler>> SPELL_HANDLER = ATTACHMENT_TYPES.register(
            "spell_handler", () -> AttachmentType.serializable(SpellHandler::new).copyOnDeath().build());
    public static final Supplier<AttachmentType<SkillHandler>> SKILL_HANDLER = ATTACHMENT_TYPES.register(
            "skill_handler", () -> AttachmentType.serializable(SkillHandler::new).copyOnDeath().build());
    public static final Supplier<AttachmentType<EffectHandler>> STATUS_EFFECTS = ATTACHMENT_TYPES.register(
            "effect_handler", () -> AttachmentType.serializable(EffectHandler::new).build());

    //Mana
    public static final Supplier<AttachmentType<Double>> MANA = ATTACHMENT_TYPES.register(
            "mana", () -> AttachmentType.builder(() -> 100d).serialize(Codec.DOUBLE).build());

    //Upgrade Tree
    public static final Supplier<AttachmentType<UpgradeTree>> UPGRADE_TREE = ATTACHMENT_TYPES.register(
            "upgrade_tree", () -> AttachmentType.serializable(UpgradeTree::new).copyOnDeath().build());

    //Summons
    public static final Supplier<AttachmentType<Integer>> OWNER_ID = ATTACHMENT_TYPES.register(
            "owner_id", () -> AttachmentType.builder(() -> 0).build());
    public static final Supplier<AttachmentType<Integer>> TARGET_ID = ATTACHMENT_TYPES.register(
            "target_id", () -> AttachmentType.builder(() -> 0).build());

    //Spell Data
    public static final Supplier<AttachmentType<Integer>> HEAT_TICK = ATTACHMENT_TYPES.register(
            "heat_tick", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
    public static final Supplier<AttachmentType<Integer>> STORMSTRIKE_OWNER = ATTACHMENT_TYPES.register(
            "stormstrike_owner", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
    public static final Supplier<AttachmentType<Boolean>> STORMSTRIKE_FLAG = ATTACHMENT_TYPES.register(
            "stormstrike_flag", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());

    //Components
    public static final Supplier<DataComponentType<String>> SPELL = COMPONENT_TYPES.registerComponentType("spell",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static void register(IEventBus modEventBus) {
        DataInit.ATTACHMENT_TYPES.register(modEventBus);
        DataInit.COMPONENT_TYPES.register(modEventBus);
    }
}
