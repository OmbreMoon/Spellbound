package com.ombremoon.spellbound.common.magic.skills;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Mth;

import java.util.Iterator;
import java.util.Map;

public class SkillCooldowns {
    private final Map<Skill, Instance> cooldowns = new Object2ObjectOpenHashMap<>();
    private int tickCount;

    public boolean isOnCooldown(Skill skill) {
        return this.getCooldownPercent(skill, 0.0F) > 0.0F;
    }

    public float getCooldownPercent(Skill skill, float partialTicks) {
        Instance instance = this.cooldowns.get(skill);
        if (instance != null) {
            float f = instance.endTime - instance.startTime;
            float f1 = instance.endTime - (this.tickCount + partialTicks);
            return Mth.clamp(f1 / f, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public void tick() {
        this.tickCount++;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Map.Entry<Skill, Instance>> iterator = this.cooldowns.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Skill, Instance> entry = iterator.next();
                if (entry.getValue().endTime <= this.tickCount) {
                    iterator.remove();
                    this.onCooldownEnded(entry.getKey());
                }
            }

        }
    }

    public void addCooldown(Skill skill, int ticks) {
        this.cooldowns.put(skill, new Instance(this.tickCount, this.tickCount + ticks));
        this.onCooldownStarted(skill, ticks);
    }

    public void removeCooldown(Skill skill) {
        this.cooldowns.remove(skill);
        this.onCooldownEnded(skill);
    }

    public void onCooldownStarted(Skill skill, int ticks) {}

    public void onCooldownEnded(Skill skill) {}

    record Instance(int startTime, int endTime) {}
}
