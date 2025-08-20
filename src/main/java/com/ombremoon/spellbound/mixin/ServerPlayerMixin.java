package com.ombremoon.spellbound.mixin;

import com.ombremoon.spellbound.common.init.SBTriggers;
import com.ombremoon.spellbound.main.Constants;
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo info) {
//        if (self().getHealth() == 1) {
//            this.hasOrHadOneHeart = true;
//            this.hasOneHeartTick = self().tickCount;
//        }
//
//        if (this.hasOrHadOneHeart)
//            SBTriggers.HEAL_TO_FULL.get().trigger(self(), 1);
//
//        if (self().tickCount > this.hasOneHeartTick + 200)
//            this.hasOrHadOneHeart = false;
    }

    private ServerPlayer self() {
        return (ServerPlayer)(Object)this;
    }
}
