package com.ombremoon.spellbound.common.magic;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.magic.events.SpellEvent;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.function.Consumer;

public class SpellEventListener {
    private final Multimap<IEvent, EventInstance<? extends SpellEvent>> events = ArrayListMultimap.create();
    private final Player player;

    public SpellEventListener(Player player) {
        this.player = player;
    }

    public <T extends SpellEvent> void addListener(IEvent event, UUID uuid, Consumer<T> consumer) {
        if (!isProperSide(event)) return;
        this.removeListener(event, uuid);
        this.events.put(event, new EventInstance<>(uuid, consumer));
    }

    public void removeListener(IEvent event, UUID uuid) {
        var instances = this.events.get(event);
        instances.removeIf(eventInstance -> eventInstance.uuid().equals(uuid));
    }

    private boolean isProperSide(IEvent event) {
        return this.player.level().isClientSide == event.isClientSide();
    }

    @SuppressWarnings("unchecked")
    public <T extends SpellEvent> boolean fireEvent(IEvent event, T spellEvent) {
        if (!isProperSide(event)) return false;

        var instances = this.events.get(event);
        boolean flag = false;
        for (var instance : instances) {
            var consumer = (Consumer<T>) instance.spellConsumer();
            consumer.accept(spellEvent);
            flag = spellEvent.isCancelled();
        }
        return flag;
    }

    public static class Event implements IEvent {
        public static Event JUMP = new Event(false);

        private final boolean isClientSide;

        Event(boolean isClientSide) {
            this.isClientSide = isClientSide;
        }

        @Override
        public boolean isClientSide() {
            return this.isClientSide;
        }
    }

    public interface IEvent {
        boolean isClientSide();
    }

    public record EventInstance<T extends SpellEvent>(UUID uuid, Consumer<T> spellConsumer) {}
}
