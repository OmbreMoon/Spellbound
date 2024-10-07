package com.ombremoon.spellbound.common.content.spell.ruin;

import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import com.ombremoon.spellbound.common.init.DataTypeInit;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.UnknownNullability;

public class SolarRaySpell extends ChanneledSpell {
    protected static final SpellDataKey<Integer> SOLAR_RAY = SyncedSpellData.define(SolarRaySpell.class, DataTypeInit.INT.get());

    public static Builder<ChanneledSpell> createSolarRayBuilder() {
        return createChannelledSpellBuilder().castTime(18).updateInterval(10);
    }

    public SolarRaySpell() {
        super(SpellInit.SOLAR_RAY.get(), createSolarRayBuilder());
    }

    private int solarRay;

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        builder.define(SOLAR_RAY, 36);
    }

    @Override
    public void onCastStart(SpellContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (!level.isClientSide) {
            LOGGER.info("{}", this.getSpellData().get(SOLAR_RAY));
            SolarRay solarRay = EntityInit.SOLAR_RAY.get().create(level);
            if (solarRay != null) {
                this.solarRay = solarRay.getId();
                solarRay.setOwner(player);
                solarRay.setPos(player.position());
                solarRay.setYRot(player.getYRot());
                level.addFreshEntity(solarRay);
            }
        }
    }

    @Override
    public void onCastReset(SpellContext context) {
        LOGGER.info("{}", this.getSpellData().get(SOLAR_RAY));
        if (!context.getLevel().isClientSide) {
            SolarRay solarRay = (SolarRay) context.getLevel().getEntity(this.solarRay);
            if (solarRay != null) solarRay.discard();
        }
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        compoundTag.putInt("SolarRayId", this.solarRay);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag nbt) {
        this.solarRay = nbt.getInt("SolarRayId");
    }
}
