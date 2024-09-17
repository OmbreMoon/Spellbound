package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DebugItem extends Item {
    public DebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var handler = player.getData(DataInit.SPELL_HANDLER.get());
        if (!level.isClientSide) {
            handler.learnSpell(SpellInit.WILD_MUSHROOM_SPELL.get());
            handler.learnSpell(SpellInit.SUMMON_UNDEAD_SPELL.get());
            handler.learnSpell(SpellInit.TEST_SPELL.get());
            Constants.LOG.info("{}", handler.getActiveSpells().stream().map(spell -> spell.getSpellName().getString()).toList());
            handler.save(player);
        }
//        Constants.LOG.info("{}", handler.getSpellList().stream().map(SpellType::getResourceLocation).toList());
        return super.use(level, player, usedHand);
    }
}
