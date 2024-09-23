package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.common.init.DataInit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ManaTearItem extends Item {
    public ManaTearItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player.getData(DataInit.MAX_MANA) < 2000f) {
            player.setData(DataInit.MAX_MANA, player.getData(DataInit.MAX_MANA) + 100f);
            player.getItemInHand(usedHand).shrink(1);
        }

        return super.use(level, player, usedHand);
    }
}
