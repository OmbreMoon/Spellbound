package com.ombremoon.spellbound.common.magic.acquisition.divine.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.init.SBTriggers;
import com.ombremoon.spellbound.common.magic.acquisition.divine.ActionCriterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Optional;

public class CuredZombieVillagerTrigger extends SimpleTrigger<CuredZombieVillagerTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Zombie zombie, Villager villager) {
        LootContext lootContext = EntityPredicate.createContext(player, zombie);
        LootContext lootContext1 = EntityPredicate.createContext(player, villager);
        this.trigger(player, instance -> instance.matches(lootContext, lootContext1));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> zombie, Optional<ContextAwarePredicate> villager) implements SimpleTrigger.Instance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("zombie").forGetter(TriggerInstance::zombie),
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("villager").forGetter(TriggerInstance::villager)
                        )
                        .apply(instance, TriggerInstance::new)
        );

        public static ActionCriterion<TriggerInstance> curedZombieVillager() {
            return SBTriggers.CURED_ZOMBIE_VILLAGER.get()
                    .createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootContext zombie, LootContext villager) {
            return (this.zombie.isEmpty() || this.zombie.get().matches(villager)) && (!this.villager.isPresent() || this.villager.get().matches(zombie));
        }

        @Override
        public void validate(CriterionValidator validator) {
            Instance.super.validate(validator);
            validator.validateEntity(this.zombie, ".zombie");
            validator.validateEntity(this.villager, ".villager");
        }
    }
}
