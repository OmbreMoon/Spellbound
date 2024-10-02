package com.ombremoon.spellbound.common.data;

import com.ombremoon.spellbound.common.init.EffectInit;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public class StatusHandler implements INBTSerializable<CompoundTag> {
    private final Map<Effect, Integer> buildUp = new HashMap<>();
    private LivingEntity self;

    public void init(LivingEntity self) {
        this.self = self;
    }

    public boolean isInitialised() { return self != null; }

    public void increment(Effect effect, int amount) {
        Integer progress = buildUp.get(effect);
        if (progress == null) progress = amount;
        else progress = Math.clamp(progress + amount, 0, 100);

        buildUp.put(effect, progress);
        if (progress >= 100) tryApplyEffect(effect);
    }

    public int getBuildUp(Effect effect) {
        return buildUp.get(effect);
    }

    public void clearEffect(Effect effect) {
        buildUp.put(effect, 0);
        self.removeEffect(effect.getEffect());
    }

    public void clearAll() {
        for (Effect effect : buildUp.keySet()) {
            buildUp.put(effect, 0);
            self.removeEffect(effect.getEffect());
        }
    }

    public void tick(int tickCount) {
        if (self == null) throw new RuntimeException("Status Effects not initialised.");
        if (tickCount % 20 == 0) buildUp.replaceAll((effect, amount) -> amount > 0 ? amount-1 : 0);
    }

    private void tryApplyEffect(Effect effect) {
        if (effect.getEffect() == null) return;
        self.addEffect(new MobEffectInstance(effect.getEffect(), 60));
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {

    }

    public enum Effect {
        FIRE(EffectInit.INFLAMED),
        FROST(EffectInit.FROZEN),
        SHOCK(EffectInit.SHOCKED),
        WIND(EffectInit.WIND),
        EARTH(EffectInit.EARTH),
        POISON(EffectInit.POISON),
        DISEASE(EffectInit.DISEASE);

        private final Holder<MobEffect> mobEffect;

        Effect(Holder<MobEffect> mobEffect) {
            this.mobEffect = mobEffect;
        }

        public Holder<MobEffect> getEffect() {
            return mobEffect;
        }
    }
}
