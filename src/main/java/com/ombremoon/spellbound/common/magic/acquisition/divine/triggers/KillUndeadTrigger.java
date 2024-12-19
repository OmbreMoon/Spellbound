package com.ombremoon.spellbound.common.magic.acquisition.divine.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Optional;

public class KillUndeadTrigger extends SimpleTrigger<KillUndeadTrigger.Instance> {

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity entity, DamageSource source, int numUndead) {
        LootContext context = EntityPredicate.createContext(player, entity);
        this.trigger(player, instance -> instance.matches(player, context, source, numUndead));
    }

    public record Instance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entityPredicate,
                           Optional<DamageSourcePredicate> killingBlow,
                           MinMaxBounds.Ints undeadKilled) implements SimpleTrigger.Instance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(Instance::entityPredicate),
                        DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(Instance::killingBlow),
                        MinMaxBounds.Ints.CODEC.optionalFieldOf("num_undead_killed", MinMaxBounds.Ints.ANY).forGetter(Instance::undeadKilled)
                ).apply(instance, Instance::new)
        );

        public boolean matches(ServerPlayer player, LootContext context, DamageSource source, int numUndead) {
            if (this.killingBlow.isPresent() && !this.killingBlow.get().matches(player, source)) {
                return false;
            } else {
                return (this.entityPredicate.isEmpty() || this.entityPredicate.get().matches(context)) && this.undeadKilled.matches(numUndead);
            }
        }
    }
}
