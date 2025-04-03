package com.ombremoon.spellbound.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.ombremoon.spellbound.common.init.SBTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieVillager.class)
public class ZombieVillagerMixin {

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/CuredZombieVillagerTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/monster/Zombie;Lnet/minecraft/world/entity/npc/Villager;)V"))
    private void finishConversion(ServerLevel serverLevel, CallbackInfo ci, @Local Villager villager, @Local Player player) {
        SBTriggers.CURED_ZOMBIE_VILLAGER.get().trigger((ServerPlayer) player, spellbound$self(), villager);
    }

    @Unique
    private ZombieVillager spellbound$self() {
        return (ZombieVillager) (Object) this;
    }
}
