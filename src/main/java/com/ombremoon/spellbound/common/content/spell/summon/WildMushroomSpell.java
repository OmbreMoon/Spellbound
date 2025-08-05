package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.common.content.entity.spell.WildMushroom;
import com.ombremoon.spellbound.common.init.SBDataTypes;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class WildMushroomSpell extends AnimatedSpell {
    protected static final SpellDataKey<Integer> MUSHROOM = SyncedSpellData.registerDataKey(WildMushroomSpell.class, SBDataTypes.INT.get());
    private static final ResourceLocation SYNTHESIS = CommonClass.customLocation("synthesis");
    private static final ResourceLocation RECYCLED_LOCATION = CommonClass.customLocation("recycled_regen");

    public static Builder<WildMushroomSpell> createMushroomBuilder() {
        return createSimpleSpellBuilder(WildMushroomSpell.class)
                .mastery(SpellMastery.ADEPT)
                .duration(240)
                .manaCost(30)
                .castCondition((context, spell) -> spell.hasValidSpawnPos() && context.canCastWithLevel());
    }

    public WildMushroomSpell() {
        super(SBSpells.WILD_MUSHROOM.get(), createMushroomBuilder());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        builder.define(MUSHROOM, 0);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            WildMushroom mushroom = this.summonEntity(context, SBEntities.MUSHROOM.get());
            this.setMushroom(mushroom.getId());
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            WildMushroom mushroom = this.getMushroom(context);
            if (mushroom != null) {
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, mushroom.getBoundingBox().inflate(2));

            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            WildMushroom mushroom = this.getMushroom(context);
            if (mushroom != null)
                mushroom.setEndTick(5);
        }
    }

    private void setMushroom(int cyclone) {
        this.spellData.set(MUSHROOM, cyclone);
    }

    private WildMushroom getMushroom(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.spellData.get(MUSHROOM));
        return entity instanceof WildMushroom mushroom ? mushroom : null;
    }
}
