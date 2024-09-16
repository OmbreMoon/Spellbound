package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.StatInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public class SpellTomeItem extends Item {
    private final Supplier<? extends SpellType<?>> spellType;

    public SpellTomeItem(Supplier<? extends SpellType<?>> spellType, Properties properties) {
        super(properties.stacksTo(1));
        this.spellType = spellType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            var handler = SpellUtil.getSpellHandler(player);
            if (handler.getSpellList().contains(this.spellType.get())) {
                //NOTIFY FAILURE
                player.getData(DataInit.SKILL_HANDLER.get()).awardSpellXp(this.spellType, 10);
                return InteractionResultHolder.fail(itemStack);
            }

            //NOTIFY SUCCESS
            handler.getSpellList().add(this.spellType.get());
            itemStack.shrink(1);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.awardStat(StatInit.SPELLS_LEARNED.get());
        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }
}
