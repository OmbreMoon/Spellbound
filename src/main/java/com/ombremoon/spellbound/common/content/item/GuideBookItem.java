package com.ombremoon.spellbound.common.content.item;

import com.google.common.reflect.ClassPath;
import com.ombremoon.spellbound.client.gui.GuideBookScreen;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.init.SBItems;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.acquisition.guides.GuideBookManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GuideBookItem extends Item {
    private final ResourceLocation bookId;
    private final ResourceLocation bookTexture;

    public GuideBookItem(ResourceLocation bookId) {
        this(bookId, ResourceLocation.fromNamespaceAndPath(bookId.getNamespace(), "textures/gui/books/" + bookId.getPath() + ".png"));
    }

    public GuideBookItem(ResourceLocation bookId, ResourceLocation bookTexture) {
        super(new Properties().stacksTo(1));
        this.bookId = bookId;
        this.bookTexture = bookTexture;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (this.bookId == null) return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        if (GuideBookManager.getBook(this.bookId) == null) return InteractionResultHolder.fail(player.getItemInHand(usedHand));

        if (level.isClientSide) {
            Minecraft.getInstance().setScreen(new GuideBookScreen(Component.translatable("screen.spellbound.guide_book"), this.bookId, bookTexture));
        }

        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }
}
