package com.ombremoon.spellbound.mixin;

import com.ombremoon.spellbound.common.init.SBTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    private boolean hasOrHadOneHeart;
    private int hasOneHeartTick;
    private int undeadKilled = 0;
    private int undeadKillTick;
    private int villagersKilled = 0;
    private int villagerKillTick;

    @Inject(method = "awardKillScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/KilledTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void awardKillScore(Entity killed, int scoreValue, DamageSource damageSource, CallbackInfo info) {
        SBTriggers.PLAYER_KILL.get().trigger(self(), killed, damageSource, 0);
        if (killed.getType().is(EntityTypeTags.UNDEAD)) {
            this.undeadKilled++;
            this.undeadKillTick = self().tickCount;
        }

        if (this.undeadKilled > 0)
            SBTriggers.KILL_UNDEAD.get().trigger(self(), killed, damageSource, this.undeadKilled);

        if (self().tickCount > this.undeadKillTick + 200) {
            this.undeadKilled = 1;
            this.undeadKillTick = self().tickCount;
        }

        if (killed instanceof Villager) {
            this.villagersKilled++;
            this.villagerKillTick = self().tickCount;
        }

        if (this.villagersKilled > 0)
            SBTriggers.KILL_VILLAGER.get().trigger(self(), killed, damageSource, this.villagersKilled);

        if (self().tickCount > this.villagerKillTick + 200) {
            this.villagersKilled = 1;
            this.villagerKillTick = self().tickCount;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo info) {
        if (self().getHealth() == 1) {
            this.hasOrHadOneHeart = true;
            this.hasOneHeartTick = self().tickCount;
        }

        if (this.hasOrHadOneHeart)
            SBTriggers.HEAL_TO_FULL.get().trigger(self(), 1);

        if (self().tickCount > this.hasOneHeartTick + 200)
            this.hasOrHadOneHeart = false;
    }

    private ServerPlayer self() {
        return (ServerPlayer)(Object)this;
    }
}
