package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.common.init.SBAttributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
        AttributeInstance attributeInstance = player.getAttribute(SBAttributes.MAX_MANA);
        attributeInstance.setBaseValue(attributeInstance.getBaseValue() + 100d);

        return super.use(level, player, usedHand);
    }
}
