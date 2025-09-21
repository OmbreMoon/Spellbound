package com.ombremoon.spellbound.common.magic.acquisition.divine.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.init.SBTriggers;
import com.ombremoon.spellbound.common.magic.acquisition.divine.ActionCriterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SpecialTrigger extends SimpleTrigger<SpecialTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, p_222625_ -> true);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleTrigger.Instance {
        public static final Codec<SpecialTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                p_337390_ -> p_337390_.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SpecialTrigger.TriggerInstance::player))
                        .apply(p_337390_, SpecialTrigger.TriggerInstance::new)
        );

        public static ActionCriterion<TriggerInstance> decoratedShrine() {
            return SBTriggers.DECORATED_SHRINE.get().createCriterion(new TriggerInstance(Optional.empty()));
        }
    }
}
