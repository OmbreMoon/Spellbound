package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.client.shader.SBShaders;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockIndex;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockManager;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DebugItem extends Item implements Loggable {
    public DebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var handler = SpellUtil.getSpellHandler(player);
        var skillHandler = SpellUtil.getSkillHolder(player);
        ombreDebug(level, player, usedHand, handler, skillHandler);
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        MultiblockManager.byKey(CommonClass.customLocation("test")).value().debugMultiblock(level, context.getClickedPos(), context.getHorizontalDirection());
        return super.useOn(context);
    }

    private void ombreDebug(Level level, Player player, InteractionHand usedHand, SpellHandler spellHandler, SkillHolder skillHolder) {
        if (!level.isClientSide) {
//            skillHolder.awardSpellXp(SBSpells.STRIDE.get(), 1500);
//            Constants.LOG.info("{}", );
            Constants.LOG.info("{}", MultiblockManager.byKey(CommonClass.customLocation("test")).value().indices);
        } else {
//            SBShaders.HEAT_DISTORTION_SHADER.toggleShader();
        }
    }
}
