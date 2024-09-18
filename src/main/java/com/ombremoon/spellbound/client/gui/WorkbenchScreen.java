package com.ombremoon.spellbound.client.gui;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class WorkbenchScreen extends Screen {
    private static final ResourceLocation TEXTURE = CommonClass.customLocation("textures/gui/arcane_gui.png");
    private static final int MAX = 4;
    private static final int WIDTH = 230;
    private static final int HEIGHT = 166;
    private int selectedIndex = -1;
    private SpellType<?> selectedSpell;
    private int pageIndex;
    private int scrollIndex;
    private int leftPos;
    private int topPos;

    public WorkbenchScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < 4; i++) {
            if (isHovering(5, 26 + (i * 28), 73, 28, mouseX, mouseY) && i != selectedIndex) {
                this.selectedIndex = i;
                var handler = Minecraft.getInstance().player.getData(DataInit.SPELL_HANDLER);
                var spellList = handler.getSpellList().stream().filter(spellType -> spellType.getPath().ordinal() == pageIndex).toList();
                if (spellList.size() > i + scrollIndex) {
                    this.selectedSpell = spellList.get(i + scrollIndex);
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            if (isHovering(80 + (i * 26), -30, 26, 30, mouseX, mouseY) && i != pageIndex) {
                this.pageIndex = i;
                this.selectedIndex = -1;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Player player = Minecraft.getInstance().player;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderUtil.setupScreen(TEXTURE);
        this.renderBg(guiGraphics, player, this.leftPos, this.topPos, mouseX, mouseY, partialTick);
        this.renderTabs(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private void renderBg(GuiGraphics guiGraphics, Player player, int xPos, int yPos, int mouseX, int mouseY, float partialTick) {
        int textColor = 16777215;
        guiGraphics.blit(TEXTURE, xPos, yPos, 0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < 4; i++) {
            guiGraphics.blit(TEXTURE, xPos + 5, yPos + 26 + (i * 28), 110, 228, 73, 28);
            var spellHandler = player.getData(DataInit.SPELL_HANDLER);
            var spellList = spellHandler.getSpellList().stream().filter(spellType -> spellType.getPath().ordinal() == pageIndex).toList();
            if (spellList.size() > i + scrollIndex) {
                var spellType = spellList.get(i + scrollIndex);
                if (spellType != null) {
                    if (isHovering(5, 26 + (i * 28), 73, 28, mouseX, mouseY) || i == selectedIndex) {
                        guiGraphics.blit(TEXTURE, xPos + 5, yPos + 26 + (i * 28), 183, 228, 73, 28);
                    } else {
                        guiGraphics.blit(TEXTURE, xPos + 5, yPos + 26 + (i * 28), 183, 200, 73, 28);
                    }
                    guiGraphics.drawCenteredString(this.font, spellType.createSpell().getSpellName(), xPos + 41, yPos + 36 + (i * 28), textColor);
                }
            }
        }
        var skillHandler = player.getData(DataInit.SKILL_HANDLER);
        guiGraphics.drawCenteredString(this.font, this.selectedSpell != null ? this.selectedSpell.createSpell().getSpellName() : Component.empty(), xPos + 62, yPos + 148, textColor);
        guiGraphics.drawCenteredString(this.font, this.selectedSpell != null ? Component.literal(String.valueOf((int)skillHandler.getSpellLevel(selectedSpell))) : Component.empty(), xPos + 14, yPos + 148, textColor);
        guiGraphics.drawCenteredString(this.font, SpellPath.values()[pageIndex].toString(), xPos + 41, yPos + 8, SpellPath.values()[pageIndex].getColor());
    }

    private void renderTabs(GuiGraphics guiGraphics, int xPos, int yPos, int mouseX, int mouseY) {
        for (int i = 0; i < 5; i++) {
            guiGraphics.blit(TEXTURE, xPos + 80 + (i * 26), yPos - 30, 0, 194, 26, 30);
            if (isHovering(80 + (i * 26), -30, 26, 30, mouseX, mouseY) || i == pageIndex) {
                guiGraphics.blit(TEXTURE, xPos + 80 + (i * 26), yPos - 32, 0, 224, 26, 32);
            }
        }
    }

    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        double d0 = pMouseX - (double)(i + pX);
        double d1 = pMouseY - (double)(j + pY);
        return d0 >= 0 && d1 >= 0 && d0 < pWidth && d1 < pHeight;
    }
}
