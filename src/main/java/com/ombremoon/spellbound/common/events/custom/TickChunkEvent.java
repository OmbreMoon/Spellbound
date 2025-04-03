package com.ombremoon.spellbound.common.events.custom;

import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.Event;

public abstract class TickChunkEvent extends Event {
    private final LevelChunk chunk;
    private final int randomTickSpeed;

    public TickChunkEvent(LevelChunk chunk, int randomTickSpeed) {
        this.chunk = chunk;
        this.randomTickSpeed = randomTickSpeed;
    }

    public LevelChunk getChunk() {
        return this.chunk;
    }

    public int getRandomTickSpeed() {
        return this.randomTickSpeed;
    }

    public static class Pre extends TickChunkEvent {

        public Pre(LevelChunk chunk, int randomTickSpeed) {
            super(chunk, randomTickSpeed);
        }
    }

    public static class Post extends TickChunkEvent {

        public Post(LevelChunk chunk, int randomTickSpeed) {
            super(chunk, randomTickSpeed);
        }
    }
}
