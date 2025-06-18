package com.ombremoon.spellbound.common.magic.acquisition.divine;

import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.common.init.SBTriggers;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.LinkedHashMap;
import java.util.Map;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class PlayerDivineActions implements Loggable {
    private final Map<ActionHolder, ActionProgress> progress = new LinkedHashMap<>();
    private final Map<ActionHolder, Integer> recentActions = new Object2IntOpenHashMap<>();
    private final ServerPlayer player;


    public PlayerDivineActions(ServerPlayer player) {
        this.player = player;
        this.registerListeners();
    }

    public void stopListening() {
        for (var trigger : SBTriggers.REGISTRY) {
            trigger.removePlayerListener(this);
        }
    }

    public void reload() {
        this.stopListening();
        this.progress.clear();
        this.recentActions.clear();
        this.registerListeners();
    }

    private void registerListeners() {
        for (ActionHolder actionHolder : DivineActionManager.getAllActions()) {
            this.registerListeners(actionHolder);
        }
    }

    public void award(ActionHolder action, String key) {
        var progress = this.getOrStartProgress(action);
        boolean flag2 = progress.isDone();
        if (this.recentActions.containsKey(action) && this.player.tickCount < this.recentActions.get(action))
            return;

        if (progress.grantProgress(key)) {
            this.unregisterListeners(action);
            if (!flag2 && progress.isDone()) {
                action.value().rewards().grant(this.player);
                this.recentActions.put(action, this.player.tickCount + 600);
                for (String s : progress.getCompletedCriteria()) {
                    this.revoke(action, s);
                }
            }
        }
    }

    public boolean revoke(ActionHolder action, String key) {
        boolean flag = false;
        var progress = this.getOrStartProgress(action);
        if (progress.revokeProgress(key)) {
            this.registerListeners(action);
            flag = true;
        }
        return flag;
    }

    private void registerListeners(ActionHolder action) {
        ActionProgress progress = this.getOrStartProgress(action);
        if (!progress.isDone()) {
            for (var entry : action.value().criteria().entrySet()) {
                var criterionProgress = progress.getCriterion(entry.getKey());
                if (criterionProgress != null && !criterionProgress.isDone())
                    this.registerListener(action, entry.getKey(), entry.getValue());
            }
        }
    }

    private <T extends CriterionTriggerInstance> void registerListener(ActionHolder action, String key, ActionCriterion<T> criterion) {
        criterion.trigger().addPlayerListener(this, new ActionTrigger.Listener<>(criterion.triggerInstance(), action, key));
    }

    private void unregisterListeners(ActionHolder action) {
        var progress = this.getOrStartProgress(action);

        for (var entry : action.value().criteria().entrySet()) {
            var criterionProgress = progress.getCriterion(entry.getKey());
            if (criterionProgress != null && (criterionProgress.isDone() || progress.isDone()))
                this.removeListener(action, entry.getKey(), entry.getValue());
        }
    }

    private <T extends CriterionTriggerInstance> void removeListener(ActionHolder action, String key, ActionCriterion<T> criterion) {
        criterion.trigger().removePlayerListener(this, new ActionTrigger.Listener<>(criterion.triggerInstance(), action, key));
    }

    public ActionProgress getOrStartProgress(ActionHolder action) {
        var progress = this.progress.get(action);
        if (progress == null) {
            progress = new ActionProgress();
            this.startProgress(action, progress);
        }
        return progress;
    }

    private void startProgress(ActionHolder action, ActionProgress progress) {
        progress.update(action.value().requirements());
        this.progress.put(action, progress);
    }

    @SubscribeEvent
    public static void onDataReload(OnDatapackSyncEvent event) {
        for (Player player : event.getRelevantPlayers().toList()) {
            var handler = SpellUtil.getSpellCaster(player);
            var actions = handler.getDivineActions();
            actions.reload();
        }
    }
}
