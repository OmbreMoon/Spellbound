package com.ombremoon.spellbound.common.events.custom;

import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import net.neoforged.bus.api.Event;

public abstract class BuildSpellEvent extends Event {
    private final SpellType<?> spellType;
    private final AbstractSpell.Builder<?> builder;

    public BuildSpellEvent(SpellType<?> spellType, AbstractSpell.Builder<? extends AbstractSpell> builder) {
        this.spellType = spellType;
        this.builder = builder;
    }

    public SpellType<?> getSpellType() {
        return this.spellType;
    }

    public AbstractSpell.Builder<?> getBuilder() {
        return this.builder;
    }

    public static class Animated extends BuildSpellEvent {
        private final SpellType<?> spellType;
        private final AnimatedSpell.Builder<?> builder;

        public Animated(SpellType<?> spellType, AnimatedSpell.Builder<?> builder) {
            super(spellType, builder);
            this.spellType = spellType;
            this.builder = builder;
        }

        public SpellType<?> getSpellType() {
            return this.spellType;
        }

        public AnimatedSpell.Builder<?> getBuilder() {
            return this.builder;
        }
    }

    public static class Channeled extends BuildSpellEvent {
        private final SpellType<?> spellType;
        private final ChanneledSpell.Builder<?> builder;

        public Channeled(SpellType<?> spellType, ChanneledSpell.Builder<?> builder) {
            super(spellType, builder);
            this.spellType = spellType;
            this.builder = builder;
        }

        public SpellType<?> getSpellType() {
            return this.spellType;
        }

        public ChanneledSpell.Builder<?> getBuilder() {
            return this.builder;
        }
    }
}
