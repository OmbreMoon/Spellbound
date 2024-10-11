package com.ombremoon.spellbound.common.content.spell.ruin.shock;

import com.ombremoon.spellbound.common.content.entity.spell.StormstrikeBolt;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StormstrikeSpell extends AnimatedSpell {
    public static Builder<StormstrikeSpell> createStormstrikeBuilder() {
        return createSimpleSpellBuilder(StormstrikeSpell.class).updateInterval(2);
    }

    public StormstrikeSpell() {
        super(SpellInit.STORMSTRIKE.get(), createStormstrikeBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (!level.isClientSide) {
            StormstrikeBolt bolt = new StormstrikeBolt(context.getLevel(), context.getPlayer(), this);
            bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            level.addFreshEntity(bolt);
        }
    }
}
