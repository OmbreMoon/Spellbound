package com.ombremoon.spellbound.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ombremoon.spellbound.client.gui.radial.RadialMenu;
import com.ombremoon.spellbound.client.gui.radial.RadialMenuItem;
import com.ombremoon.spellbound.client.gui.radial.SpellRadialMenuItem;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class SpellSelectScreen extends Screen {
    private static final int RADIAL_WIDTH = 80;
    private static final int RADIAL_HEIGHT = RADIAL_WIDTH;
    private static final int RADIAL_ITEM_WIDTH = 40;
    private static final int RADIAL_ITEM_HEIGHT = RADIAL_ITEM_WIDTH;
    private static final int BACKGROUND_COLOR = 0x3F000000;
    private static final int BACKGROUND_HOVER_COLOR = 0x3FFFFFFF;
    private final Player player;
    private final SpellHandler handler;
    private final RadialMenu radialMenu;
    private SpellRadialMenuItem[] spellItems = new SpellRadialMenuItem[5];
    private final List<RadialMenuItem> items;
    private float x;
    private float y;

    public SpellSelectScreen() {
        super(Component.literal("TEMP RADIAL SELECTION"));
        this.player = Minecraft.getInstance().player;
        this.handler = SpellUtil.getSpellHandler(this.player);
        this.items = new ObjectArrayList<>();
        this.radialMenu = new RadialMenu(this, this.items, RADIAL_WIDTH - RADIAL_ITEM_WIDTH, RADIAL_WIDTH, BACKGROUND_COLOR, BACKGROUND_HOVER_COLOR) {
            @Override
            public void onClickOutside() {
                close();
            }
        };

        for (int i = 0; i < handler.equippedSpellSet.size(); i++) {
            spellItems[i] = new SpellRadialMenuItem(radialMenu, handler.equippedSpellSet.stream().toList().get(i)) {
                @Override
                public boolean onClick() {
                    if (handler.getSelectedSpell() == this.getSpellType())
                        return false;

                    SpellType<?> spellType = this.getSpellType();
                    handler.setSelectedSpell(spellType);
                    PayloadHandler.setSpell(spellType);
                    radialMenu.close();
                    return true;
                }
            };
        }
    }

    @Override
    protected void init() {
        super.init();
        this.x = (float) this.width / 2;
        this.y = (float) this.height / 2;
        for (int i = 0; i < handler.equippedSpellSet.size(); i++) {
            this.items.add(spellItems[i]);
        }

    }

    @Override
    public void tick() {
        super.tick();

        radialMenu.tick();

        if (radialMenu.isClosed())
            Minecraft.getInstance().setScreen(null);

        if (!radialMenu.isReady())
            return;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        radialMenu.clickItem();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        poseStack.popPose();
        radialMenu.draw(guiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}