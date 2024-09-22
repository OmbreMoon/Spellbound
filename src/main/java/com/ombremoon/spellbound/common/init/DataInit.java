package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.data.StatusHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class DataInit {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID
    );

    public static final Supplier<AttachmentType<SpellHandler>> SPELL_HANDLER = ATTACHMENT_TYPES.register(
            "spell_handler", () -> AttachmentType.serializable(SpellHandler::new).copyOnDeath().build());
    public static final Supplier<AttachmentType<SkillHandler>> SKILL_HANDLER = ATTACHMENT_TYPES.register(
            "skill_handler", () -> AttachmentType.serializable(SkillHandler::new).copyOnDeath().build());
    public static final Supplier<AttachmentType<StatusHandler>> STATUS_EFFECTS = ATTACHMENT_TYPES.register(
            "effect_handler", () -> AttachmentType.serializable(StatusHandler::new).build());

    public static final Supplier<AttachmentType<Float>> MANA = ATTACHMENT_TYPES.register(
            "mana", () -> AttachmentType.builder(() -> 100f).build());
    public static final Supplier<AttachmentType<Float>> MAX_MANA = ATTACHMENT_TYPES.register(
            "max_mana", () -> AttachmentType.builder(() -> 100f).build());

    public static final Supplier<AttachmentType<String>> OWNER_UUID = ATTACHMENT_TYPES.register(
            "owner_uuid", () -> AttachmentType.builder(() -> "").build());
    public static final Supplier<AttachmentType<Integer>> TARGET_ID = ATTACHMENT_TYPES.register(
            "target_id", () -> AttachmentType.builder(() -> 0).build());

    public static void register(IEventBus modEventBus) {
        DataInit.ATTACHMENT_TYPES.register(modEventBus);
    }
}
