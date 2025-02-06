package com.ombremoon.spellbound.common.content.entity.living;

import com.ombremoon.spellbound.common.content.world.SBTrades;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

public abstract class SBMerchant extends PathfinderMob implements Merchant {
    @Nullable
    private Player tradingPlayer;
    public MerchantOffers trades = new MerchantOffers();

    protected SBMerchant(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            this.startTrading(player);
        }
        return super.mobInteract(player, hand);
    }

    private void startTrading(Player player) {
        this.setTradingPlayer(player);
        System.out.println(player);
        this.openTradingScreen(player, this.getDisplayName(), 1);
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
        System.out.println(player);
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public void overrideOffers(MerchantOffers merchantOffers) {
        this.trades = merchantOffers;
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int i) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }
}
