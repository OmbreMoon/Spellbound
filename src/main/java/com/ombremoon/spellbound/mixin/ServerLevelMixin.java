package com.ombremoon.spellbound.mixin;

import com.ombremoon.spellbound.common.events.custom.TickChunkEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "tickChunk", at = @At("HEAD"))
    private void tickChunkPre(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new TickChunkEvent.Pre(chunk, randomTickSpeed));
    }

    @Inject(method = "tickChunk", at = @At("TAIL"))
    private void tickChunkPost(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new TickChunkEvent.Post(chunk, randomTickSpeed));
    }
}
