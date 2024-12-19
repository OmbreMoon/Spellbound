package com.ombremoon.spellbound.common.magic.acquisition.divine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.advancements.CriterionProgress;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActionProgress {
    private final Map<String, CriterionProgress> criteria;
    private ActionRequirements requirements = ActionRequirements.EMPTY;

    private ActionProgress(Map<String, CriterionProgress> criteria) {
        this.criteria = criteria;
    }

    public ActionProgress() {
        this.criteria = Maps.newHashMap();
    }

    public void update(ActionRequirements requirements) {
        Set<String > set = requirements.names();
        this.criteria.entrySet().removeIf(entry -> !set.contains(entry.getKey()));

        for (String s : set) {
            this.criteria.putIfAbsent(s, new CriterionProgress());
        }

        this.requirements = requirements;
    }

    public boolean isDone() {
        return this.requirements.test(this::isCriterionDone);
    }

    public boolean hasProgress() {
        for (var progress : this.criteria.values()) {
            if (progress.isDone()) return true;
        }
        return false;
    }

    public boolean grantProgress(String name) {
        var progress = this.getCriterion(name);
        if (progress != null && !progress.isDone()) {
            progress.grant();
            return true;
        } else {
            return false;
        }
    }

    public boolean revokeProgress(String name) {
        var progress = this.getCriterion(name);
        if (progress != null && progress.isDone()) {
            progress.revoke();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "DivineActionProgress{criteria=" + this.criteria + ", requirements=" + this.requirements + " }";
    }

    @Nullable
    public CriterionProgress getCriterion(String name) {
        return this.criteria.get(name);
    }

    private boolean isCriterionDone(String name) {
        var progress = this.getCriterion(name);
        return progress != null && progress.isDone();
    }

    public Iterable<String> getCompletedCriteria() {
        List<String> list = Lists.newArrayList();

        for (var entry : this.criteria.entrySet()) {
            if (entry.getValue().isDone()) {
                list.add(entry.getKey());
            }
        }

        return list;
    }
}
