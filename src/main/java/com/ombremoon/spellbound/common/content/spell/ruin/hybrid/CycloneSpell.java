package com.ombremoon.spellbound.common.content.spell.ruin.hybrid;

import com.ombremoon.spellbound.common.content.world.HailstormSavedData;
import com.ombremoon.spellbound.common.content.entity.spell.Cyclone;
import com.ombremoon.spellbound.common.init.SBDataTypes;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CycloneSpell extends AnimatedSpell {
    protected static final SpellDataKey<Integer> CYCLONE = SyncedSpellData.registerDataKey(CycloneSpell.class, SBDataTypes.INT.get());

    private static Builder<CycloneSpell> createCycloneBuilder() {
        return createSimpleSpellBuilder(CycloneSpell.class)
                .duration(context -> 600)
                .castCondition((context, cycloneSpell) -> cycloneSpell.getSpawnPos(100) != null)
                .updateInterval(1);
    }

    public CycloneSpell() {
        super(SBSpells.CYCLONE.get(), createCycloneBuilder());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        builder.define(CYCLONE, 0);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Level level = context.getLevel();
        if (!level.isClientSide) {
            summonEntity(context, SBEntities.CYCLONE.get(), 100, cyclone -> this.setCyclone(cyclone.getId()));

            if (context.getSkills().hasSkill(SBSkills.HAILSTORM.value())) {
                HailstormSavedData data = ((HailstormSavedData) HailstormSavedData.get(level));
                data.toggleHailing((ServerLevel) level, this.getDuration());
            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Level level = context.getLevel();
        Cyclone cyclone = this.getCyclone(context);
        if (cyclone == null) {
            if (!level.isClientSide)
                endSpell();
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            Cyclone cyclone = this.getCyclone(context);
            if (cyclone != null) {
                cyclone.setEndTick(20);
                if (cyclone.getControllingPassenger() instanceof LivingEntity livingEntity)
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * cyclone.getStacks()));
            }
        }
    }

    private void setCyclone(int cyclone) {
        this.spellData.set(CYCLONE, cyclone);
    }

    private Cyclone getCyclone(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.spellData.get(CYCLONE));
        return entity instanceof Cyclone cyclone ? cyclone : null;
    }
}
