package com.ombremoon.spellbound.common.magic;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ombremoon.spellbound.common.magic.events.SpellEvent;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class SpellEventListener {
    public final Multimap<IEvent, EventInstance<? extends SpellEvent>> events = ArrayListMultimap.create();
    public final Map<EventInstance<? extends SpellEvent>, Integer> timedEvents = new Object2IntOpenHashMap<>();
    private final Player player;

    public SpellEventListener(Player player) {
        this.player = player;
    }

    public <T extends SpellEvent> void addListener(IEvent event, UUID uuid, Consumer<T> consumer) {
        if (checkSide(event)) return;
        this.events.put(event, new EventInstance<>(uuid, consumer));
    }

    public <T extends SpellEvent> void addListenerWithExpiry(IEvent event, UUID uuid, Consumer<T> consumer, int expiryTicks) {
        if (checkSide(event)) return;
        var instance = new EventInstance<>(uuid, consumer);
        this.events.put(event, instance);
        this.timedEvents.put(instance, expiryTicks);
    }

    public void removeListener(IEvent event, UUID uuid) {
        var instances = this.events.get(event);
        for (var instance : instances) {
            if (instance.uuid().equals(uuid)) instances.remove(instance);
            break;
        }
    }

    private void removeListener(EventInstance<?> eventInstance) {
        var instances = this.events.values();
        for (var instance : instances) {
            if (instance.uuid().equals(eventInstance.uuid())) instances.remove(instance);
            break;
        }
    }

    public void tickInstances() {
        for (var entry : this.timedEvents.entrySet()) {
            int i = entry.getValue();
            i--;
            if (i > 0) {
                this.timedEvents.replace(entry.getKey(), i);
            } else {
                this.removeListener(entry.getKey());
                this.timedEvents.remove(entry.getKey());
            }
        }
    }

    private boolean checkSide(IEvent event) {
        return this.player.level().isClientSide != event.isClientSide();
    }

    @SuppressWarnings("unchecked")
    public <T extends SpellEvent> boolean fireEvent(IEvent event, T spellEvent) {
        if (checkSide(event)) return false;

        var instances = this.events.get(event);
        boolean flag = false;
        for (var instance : instances) {
            var consumer = (Consumer<T>) instance.spellConsumer();
            consumer.accept(spellEvent);
            flag |= spellEvent.isCancelled();
        }
        return flag;
    }

    public static class Events implements IEvent {
        public static Events JUMP = new Events(false);
        public static Events POST_DAMAGE = new Events(false);
        public static Events PRE_DAMAGE = new Events(false);
        public static Events TARGETING_EVENT = new Events(false);
        public static Events PLAYER_KILL = new Events(false);

        private final boolean isClientSide;

        Events(boolean isClientSide) {
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
