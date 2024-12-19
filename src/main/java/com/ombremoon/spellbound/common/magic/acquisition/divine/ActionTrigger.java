package com.ombremoon.spellbound.common.magic.acquisition.divine;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.CriterionTriggerInstance;

public interface ActionTrigger<T extends CriterionTriggerInstance> {
    void addPlayerListener(PlayerDivineActions playerActions, Listener<T> listener);

    void removePlayerListener(PlayerDivineActions playerActions, Listener<T> listener);

    void removePlayerListener(PlayerDivineActions playerActions);

    Codec<T> codec();

    default ActionCriterion<T> createCriterion(T triggerInstance) {
        return new ActionCriterion<>(this, triggerInstance);
    }

    record Listener<T extends CriterionTriggerInstance>(T trigger, ActionHolder action, String criterion) {
        public void run(PlayerDivineActions playerActions) {
            playerActions.award(this.action, this.criterion);
        }
    }
}
