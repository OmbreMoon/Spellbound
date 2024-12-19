package com.ombremoon.spellbound.common.magic.acquisition.divine;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public record ActionRequirements(List<List<String>> requirements) {
    public static final Codec<ActionRequirements> CODEC = Codec.STRING
            .listOf()
            .listOf()
            .xmap(ActionRequirements::new, ActionRequirements::requirements);
    public static final ActionRequirements EMPTY = new ActionRequirements(List.of());

    public ActionRequirements(FriendlyByteBuf buffer) {
        this(buffer.readList(buf -> buf.readList(FriendlyByteBuf::readUtf)));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(this.requirements, (buf, list) -> buf.writeCollection(list, FriendlyByteBuf::writeUtf));
    }

    public static ActionRequirements allOf(Collection<String> requirements) {
        return new ActionRequirements(requirements.stream().map(List::of).toList());
    }

    public static ActionRequirements anyOf(Collection<String> criteria) {
        return new ActionRequirements(List.of(List.copyOf(criteria)));
    }

    public int size() {
        return this.requirements.size();
    }

    public boolean test(Predicate<String> predicate) {
        if (this.requirements.isEmpty()) {
            return false;
        } else {
            for (List<String> list : this.requirements) {
                if (!anyMatch(list, predicate)) {
                    return false;
                }
            }

            return true;
        }
    }

    public int count(Predicate<String> filter) {
        int i = 0;

        for (List<String> list : this.requirements) {
            if (anyMatch(list, filter)) {
                i++;
            }
        }

        return i;
    }

    private static boolean anyMatch(List<String> requirements, Predicate<String> predicate) {
        for (String s : requirements) {
            if (predicate.test(s)) {
                return true;
            }
        }

        return false;
    }

    public DataResult<ActionRequirements> validate(Set<String> requirements) {
        Set<String> set = new ObjectOpenHashSet<>();

        for (List<String> list : this.requirements) {
            if (list.isEmpty() && requirements.isEmpty()) {
                return DataResult.error(() -> "Requirement entry cannot be empty");
            }

            set.addAll(list);
        }

        if (!requirements.equals(set)) {
            Set<String> set1 = Sets.difference(requirements, set);
            Set<String> set2 = Sets.difference(set, requirements);
            return DataResult.error(
                    () -> "Divine action completion requirements did not exactly match specified criteria. Missing: " + set1 + ". Unknown: " + set2
            );
        } else {
            return DataResult.success(this);
        }
    }

    public boolean isEmpty() {
        return this.requirements.isEmpty();
    }

    @Override
    public String toString() {
        return this.requirements.toString();
    }

    public Set<String> names() {
        Set<String> set = new ObjectOpenHashSet<>();

        for (List<String> list : this.requirements) {
            set.addAll(list);
        }

        return set;
    }

    public interface Strategy {
        ActionRequirements.Strategy AND = ActionRequirements::allOf;
        ActionRequirements.Strategy OR = ActionRequirements::anyOf;

        ActionRequirements create(Collection<String> criteria);
    }
}
