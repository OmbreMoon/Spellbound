package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.common.content.block.entity.TransfigurationDisplayBlockEntity;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockManager;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockPart;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DebugItem extends Item implements Loggable {
    public DebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var handler = SpellUtil.getSpellCaster(player);
        var skillHandler = SpellUtil.getSkills(player);
        ombreDebug(level, player, usedHand, handler, skillHandler);
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!level.isClientSide) {
            var multiblock = MultiblockManager.byKey(CommonClass.customLocation("one_ring")).value();
            multiblock.tryCreateMultiblock(level, context.getPlayer(), context.getClickedPos(), Direction.NORTH);
        }
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
