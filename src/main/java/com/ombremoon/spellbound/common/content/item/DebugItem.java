package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.client.shader.SBShaders;
import com.ombremoon.spellbound.common.content.world.multiblock.BuildingBlock;
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
//        Constants.LOG.info("{}", MultiblockManager.byKey(CommonClass.customLocation("building_block_test")).value().getBlock(MultiblockIndex.of(1, 0, 1)));
        MultiblockManager.byKey(CommonClass.customLocation("building_block_test")).value().debugMultiblock(level, context.getClickedPos(), context.getHorizontalDirection());
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void ombreDebug(Level level, Player player, InteractionHand usedHand, SpellHandler spellHandler, SkillHolder skillHolder) {
        if (!level.isClientSide) {
//            skillHolder.awardSpellXp(SBSpells.STRIDE.get(), 1500);
//            Constants.LOG.info("{}", );
//            var test = MultiblockManager.byKey(CommonClass.customLocation("building_block_test")).value().indices;
//            for (var entry : test.entrySet()) {
//                var value = entry.getValue().getValues()[0];
//                if (value instanceof BuildingBlock.BlockValue blockValue)
//                    Constants.LOG.info("{} {}", blockValue, entry.getKey());
//            }
        } else {
//            SBShaders.HEAT_DISTORTION_SHADER.toggleShader();

        }
    }
}
