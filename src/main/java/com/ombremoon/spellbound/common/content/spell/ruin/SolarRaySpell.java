package com.ombremoon.spellbound.common.content.spell.ruin;

import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SolarRaySpell extends ChanneledSpell {
    public static Builder<ChanneledSpell> createSolarRayBuilder() {
        return createChannelledSpellBuilder().castTime(18);
    }

    private SolarRay solarRay;

    public SolarRaySpell() {
        super(SpellInit.SOLAR_RAY.get(), createSolarRayBuilder());
    }

    @Override
    public void onCastStart(SpellContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        this.solarRay = EntityInit.SOLAR_RAY.get().create(level);
        if (!level.isClientSide) {
            if (this.solarRay != null) {
                this.solarRay.setOwner(player);
                this.solarRay.setPos(player.position());
                this.solarRay.setYRot(player.getYRot());
                level.addFreshEntity(this.solarRay);
            }
        }
    }

    @Override
    public void onCastReset(SpellContext context) {
        if (this.solarRay != null) this.solarRay.discard();
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
}
