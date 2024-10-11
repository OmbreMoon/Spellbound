package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.common.magic.events.*;
import com.ombremoon.spellbound.common.magic.tree.SkillNode;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class SpellEventListener {
    public final Map<IEvent<?>, List<EventInstance<? extends SpellEvent>>> events = new Object2ObjectOpenHashMap<>();
    public final Map<EventInstance<? extends SpellEvent>, Integer> timedEvents = new Object2IntOpenHashMap<>();
    private final Player player;

    public SpellEventListener(Player player) {
        this.player = player;
    }

    public boolean hasListener(IEvent<?> event, UUID uuid) {
        if (this.events.containsKey(event)) {
            for (var instance : this.events.get(event)) {
                if (instance.uuid().equals(uuid)) return true;
            }
        }
        return false;
    }
    
    public <T extends SpellEvent> void addListener(IEvent<T> event, UUID uuid, Consumer<T> consumer) {
        if (checkSide(event)) return;
        var list = refreshInstances(event, uuid, consumer);
        this.events.put(event, list);
    }

    public <T extends SpellEvent> void addListenerWithExpiry(IEvent<T> event, UUID uuid, Consumer<T> consumer, int expiryTicks) {
        if (checkSide(event)) return;
        var instance = new EventInstance<>(event, uuid, consumer);
        var list = refreshInstances(event, instance);
        this.events.put(event, list);
        this.timedEvents.put(instance, expiryTicks);
    }

    public void removeListener(IEvent<?> event, UUID uuid) {
        if (hasListener(event, uuid)) {
            var instances = this.events.get(event);
            for (var instance : instances) {
                if (instance.uuid().equals(uuid)) {
                    instances.remove(instance);
                    if (!instances.isEmpty()) {
                        this.events.replace(event, instances);
                    } else {
                        this.events.remove(event);
                    }
                    break;
                }
            }
        }
    }

    private <T extends SpellEvent> void removeListener(EventInstance<T> eventInstance) {
        var event = eventInstance.event();
        var uuid = eventInstance.uuid();
        if (hasListener(event, uuid)) {
            var instances = this.events.get(event);
            instances.remove(eventInstance);
            if (!instances.isEmpty()) {
                this.events.replace(event, instances);
            } else {
                this.events.remove(event);
            }
        }
    }

    public void tickInstances() {
        if (!this.timedEvents.isEmpty()) {
            for (var entry : this.timedEvents.entrySet()) {
                if (entry.getValue() <= this.player.tickCount) {
                    this.removeListener(entry.getKey());
                    this.timedEvents.remove(entry.getKey());
                }
            }
        }
    }

    private boolean checkSide(IEvent<?> event) {
        return this.player.level().isClientSide != event.isClientSide();
    }

    private <T extends SpellEvent> List<EventInstance<? extends SpellEvent>> refreshInstances(IEvent<T> event, EventInstance<T> instance) {
        var list = this.events.get(event);
        if (list == null) list = new ObjectArrayList<>();
        list.add(instance);
        return list;
    }

    private <T extends SpellEvent> List<EventInstance<? extends SpellEvent>> refreshInstances(IEvent<T> event, UUID uuid, Consumer<T> consumer) {
        return refreshInstances(event, new EventInstance<>(event, uuid, consumer));
    }

    @SuppressWarnings("unchecked")
    public <T extends SpellEvent> boolean fireEvent(IEvent<?> event, T spellEvent) {
        if (checkSide(event)) return false;

        var instances = this.events.get(event);
        boolean flag = false;
        if (instances != null) {
            for (var instance : instances) {
                var consumer = (Consumer<T>) instance.spellConsumer();
                consumer.accept(spellEvent);
                flag |= spellEvent.isCancelled();
            }
        }
        return flag;
    }

    public static class Events<T extends SpellEvent> implements IEvent<T> {
        public static Events<PlayerJumpEvent> JUMP = new Events<>(false);
        public static Events<PlayerAttackEvent> ATTACK = new Events<>(false);
        public static Events<PlayerDamageEvent.Post> POST_DAMAGE = new Events<>(false);
        public static Events<PlayerDamageEvent.Pre> PRE_DAMAGE = new Events<>(false);
        public static Events<ChangeTargetEvent> CHANGE_TARGET = new Events<>(false);
        public static Events<PlayerKillEvent> PLAYER_KILL = new Events<>(false);
        public static Events<MouseInputEvent.Pre> PRE_MOUSE_INPUT = new Events<>(true);
        public static Events<MouseInputEvent.Post> POST_MOUSE_INPUT = new Events<>(true);

        private final boolean isClientSide;

        Events(boolean isClientSide) {
            this.isClientSide = isClientSide;
        }

        @Override
        public boolean isClientSide() {
            return this.isClientSide;
        }
    }

    public interface IEvent<T extends SpellEvent> {
        boolean isClientSide();
    }

    public record EventInstance<T extends SpellEvent>(IEvent<T> event, UUID uuid, Consumer<T> spellConsumer) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else {
                return obj instanceof EventInstance<?> eventInstance && this.uuid.equals(eventInstance.uuid);
            }
        }
    }
}
