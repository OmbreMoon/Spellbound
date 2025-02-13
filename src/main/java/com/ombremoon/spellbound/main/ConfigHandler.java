package com.ombremoon.spellbound.main;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHandler {

    public static class Server {

    }

    public static class Common {
        public final ModConfigSpec.IntValue maxSpellLevel;
        public final ModConfigSpec.BooleanValue skillRequiresPrereqs;

        Common(ModConfigSpec.Builder builder) {
            maxSpellLevel = builder
                    .comment("The maximum number of levels any spell can obtain.")
                    .translation("spellbound.config.maxSpellLevel")
                    .defineInRange("maxSpellLevel", 5, 1, 10);

            skillRequiresPrereqs = builder
                    .comment("Set this to false if any skill can be unlocked without unlocking the necessary prerequisites.")
                    .translation("spellbound.config.skillRequiresPrereqs")
                    .define("skillRequiresPrereqs", true);
        }

    }

    public static class Client {

    }

    static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }
}
