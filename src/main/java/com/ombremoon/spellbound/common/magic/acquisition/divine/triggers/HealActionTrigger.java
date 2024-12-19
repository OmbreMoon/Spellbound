package com.ombremoon.spellbound.common.magic.acquisition.divine.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.init.SBTriggers;
import com.ombremoon.spellbound.common.magic.acquisition.divine.ActionCriterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class HealActionTrigger extends SimpleTrigger<HealActionTrigger.Instance> {

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, double startHealth) {
        this.trigger(player, instance -> instance.matches(player, startHealth));
    }

    public record Instance(Optional<ContextAwarePredicate> player, Optional<HealPredicate> healAmount) implements SimpleTrigger.Instance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                        HealPredicate.CODEC.optionalFieldOf("amount_healed").forGetter(Instance::healAmount)
                ).apply(instance, Instance::new)
        );

        public static ActionCriterion<Instance> healed(HealPredicate health) {
            return SBTriggers.HEAL_TO_FULL.get()
                    .createCriterion(new Instance(Optional.empty(), Optional.of(health)));
        }

        public boolean matches(ServerPlayer player, double startHealth) {
            return this.healAmount.isEmpty() || this.healAmount.get().matches(startHealth, player.getHealth());
        }
    }
}
