package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SpellHandler;
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
            "spell_handler", () -> AttachmentType.serializable(SpellHandler::new).build()
    );

    public static void register(IEventBus modEventBus) {
        DataInit.ATTACHMENT_TYPES.register(modEventBus);
    }
}
