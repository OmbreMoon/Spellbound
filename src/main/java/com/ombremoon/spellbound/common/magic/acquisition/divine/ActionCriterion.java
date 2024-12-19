package com.ombremoon.spellbound.common.magic.acquisition.divine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.init.SBTriggers;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.util.ExtraCodecs;

public record ActionCriterion<T extends CriterionTriggerInstance>(ActionTrigger<T> trigger, T triggerInstance) {
    private static final MapCodec<ActionCriterion<?>> MAP_CODEC = ExtraCodecs.dispatchOptionalValue(
            "trigger", "conditions", SBTriggers.REGISTRY.byNameCodec(), ActionCriterion::trigger, ActionCriterion::criterionCodec
    );
    public static final Codec<ActionCriterion<?>> CODEC = MAP_CODEC.codec();

    private static <T extends CriterionTriggerInstance> Codec<ActionCriterion<T>> criterionCodec(ActionTrigger<T> trigger) {
        return trigger.codec().xmap(triggerInstance -> new ActionCriterion<>(trigger, triggerInstance), ActionCriterion::triggerInstance);
    }
}
