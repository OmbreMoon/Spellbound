package com.ombremoon.spellbound.common.magic.acquisition.divine.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.init.SBTriggers;
import com.ombremoon.spellbound.common.magic.acquisition.divine.ActionCriterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Optional;

public class HealActionTrigger extends SimpleTrigger<HealActionTrigger.Instance> {

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, LivingEntity livingEntity, double startHealth, double endHealth) {
        LootContext context = EntityPredicate.createContext(player, livingEntity);
        this.trigger(player, instance -> instance.matches(context, startHealth, endHealth));
    }

    public record Instance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entityPredicate, Optional<HealPredicate> healAmount) implements SimpleTrigger.Instance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(Instance::entityPredicate),
                        HealPredicate.CODEC.optionalFieldOf("amount_healed").forGetter(Instance::healAmount)
                ).apply(instance, Instance::new)
        );

        public static ActionCriterion<Instance> healed(HealPredicate health) {
            return SBTriggers.ENTITY_HEALED.get()
                    .createCriterion(new Instance(Optional.empty(), Optional.empty(), Optional.of(health)));
        }

        public static ActionCriterion<Instance> healedToFull(EntityPredicate.Builder entity) {
            return SBTriggers.HEAL_TO_FULL.get()
                    .createCriterion(new Instance(Optional.empty(), Optional.of(EntityPredicate.wrap(entity)), Optional.empty()));
        }

        public boolean matches(LootContext context, double startHealth, double endHealth) {
            if (this.healAmount.isPresent() && !this.healAmount.get().matches(startHealth, endHealth)) {
                return false;
            }

            return this.entityPredicate.isEmpty() || this.entityPredicate.get().matches(context);
        }
    }
}
