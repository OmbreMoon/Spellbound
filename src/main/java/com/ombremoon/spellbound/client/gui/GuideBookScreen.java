package com.ombremoon.spellbound.client.gui;

import com.ombremoon.spellbound.common.magic.acquisition.guides.GuideBookManager;
import com.ombremoon.spellbound.common.magic.acquisition.guides.GuideBookPage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class GuideBookScreen extends Screen {
    private static final int WIDTH = 415;
    private static final int HEIGHT = 287;

    private Player player;
    private ResourceLocation bookId;
    private ResourceLocation bookTexture;
    private int leftPos;
    private int topPos;
    private int currentPage = 0;
    private int totalPages;
    private List<GuideBookPage> pages;

    public GuideBookScreen(Component title, ResourceLocation bookId) {
        super(title);
        this.bookId = bookId;
        this.bookTexture = ResourceLocation.fromNamespaceAndPath(bookId.getNamespace(), "textures/gui/books/" + bookId.getPath() + ".png");
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;
        this.pages = GuideBookManager.getBook(bookId);
        this.totalPages = this.pages.size()-1;
    }

    @Override
    protected void init() {
        this.player = this.minecraft.player;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(this.bookTexture, this.leftPos, this.topPos, 0, 0, WIDTH, HEIGHT);

        pages.get(currentPage).render(guiGraphics, this.leftPos, this.topPos);
    }
}
