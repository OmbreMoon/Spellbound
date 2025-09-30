package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockManager;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.acquisition.bosses.ArenaSavedData;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
        var skillHandler = SpellUtil.getSkills(player);
        ombreDebug(level, player, usedHand, handler, skillHandler);
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!level.isClientSide) {
            
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void ombreDebug(Level level, Player player, InteractionHand usedHand, SpellHandler spellHandler, SkillHolder skillHolder) {
        if (!level.isClientSide) {
            ArenaSavedData data = ArenaSavedData.get((ServerLevel) level);
            log(data.getCurrentBossFight());
        } else {
//            SBShaders.HEAT_DISTORTION_SHADER.toggleShader();

        }
    }
}
