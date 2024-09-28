package com.ombremoon.spellbound.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.tree.SkillNode;
import com.ombremoon.spellbound.common.magic.tree.UpgradeTree;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.RenderUtil;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class WorkbenchScreen extends Screen {
    public static final ResourceLocation TEXTURE = CommonClass.customLocation("textures/gui/arcane_gui.png");
    private static final ResourceLocation PATH = CommonClass.customLocation("textures/gui/arcane_gui_text.png");
    private static final int WIDTH = 256;
    private static final int HEIGHT = 166;
    private int selectedIndex = -1;
    private Player player;
    private SpellHandler spellHandler;
    private SkillHandler skillHandler;
    private List<SpellType<?>> spellList;
    private UpgradeTree upgradeTree;
    private final Map<SpellType<?>, UpgradeWindow> spellTrees = Maps.newLinkedHashMap();
    private SpellType<?> selectedSpell;
    private UpgradeWindow selectedTree;
    private int pageIndex;
    private boolean scrolling;
    private boolean scrollingWindow;
    private float scrollOffs;
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
        this.player = this.minecraft.player;
        this.upgradeTree = this.player.getData(DataInit.UPGRADE_TREE);
        this.spellHandler = SpellUtil.getSpellHandler(this.player);
        this.skillHandler = this.player.getData(DataInit.SKILL_HANDLER);
        this.spellList = this.spellHandler.getSpellList().stream().filter(spellType -> spellType.getPath().ordinal() == pageIndex).toList();
        this.initSpellTrees();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < 4; i++) {
            if (isHovering(19, 26 + (i * 28), 73, 28, mouseX, mouseY) && i != selectedIndex) {
                this.selectedIndex = i;
                if (this.spellList.size() > i + scrollIndex) {
                    this.selectedSpell = this.spellList.get(i + scrollIndex);
                    this.selectedTree = this.spellTrees.get(this.selectedSpell);
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            if (isHovering(92 + (i * 26), -30, 26, 30, mouseX, mouseY) && i != pageIndex) {
                this.pageIndex = i;
                this.selectedIndex = -1;
                this.scrollOffs = 0;
                this.scrollIndex = 0;
                this.spellList = this.spellHandler.getSpellList().stream().filter(spellType -> spellType.getPath().ordinal() == pageIndex).toList();
                this.selectedSpell = this.spellList.isEmpty() ? null : this.spellList.get(0);
                this.selectedTree = this.selectedSpell != null ? this.spellTrees.get(this.selectedSpell) : null;
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        if (this.selectedTree != null) {
            var widgets = this.selectedTree.widgets();
            int i = Mth.floor(this.selectedTree.scrollX);
            int j = Mth.floor(this.selectedTree.scrollY);
            for (var widget : widgets) {
                if (widget.isMouseOver(i, j, mouseX - this.leftPos - 98, mouseY - this.topPos - 18)) {
                    Skill skill = widget.getSkill();
                    if (this.skillHandler.canUnlockSkill(skill)) {
                        this.skillHandler.unlockSkill(skill);
                        PayloadHandler.unlockSkill(skill);
                    }
                }
            }
        }
        if (isHovering(5, 29 + (scrollIndex * 15), 12, 104, mouseX, mouseY)) {
            this.scrolling = true;
            return true;
        }
        if (isHovering(98, 16, 149, 115, mouseX, mouseY)) {
            this.scrollingWindow = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling) {
            int i = this.topPos + 29;
            int j = i + 89;
            this.scrollOffs = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.scrollTo(this.scrollOffs);
            this.selectedIndex = -1;
            return true;
        } else if (this.selectedTree != null && this.scrollingWindow) {
            this.selectedTree.scroll(dragX, dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        this.scrollingWindow = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isHovering(5, 29, 88, 109, mouseX, mouseY)) {
            this.scrollOffs = this.subtractInputFromScroll(this.scrollOffs, scrollY);
            this.scrollTo(this.scrollOffs);
            return true;
        } else if (this.selectedTree != null && isHovering(98, 16, 149, 115, mouseX, mouseY)) {
            if (Screen.hasShiftDown()) {
                this.selectedTree.scroll(scrollY * 16, scrollX * 16);
            } else {
                this.selectedTree.scroll(scrollX * 16, scrollY * 16);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderUtil.setupScreen(TEXTURE);
        this.renderWindow(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderBackground(guiGraphics, this.player, this.leftPos, this.topPos, mouseX, mouseY, partialTick);
        this.renderTooltips(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private void renderBackground(GuiGraphics guiGraphics, Player player, int xPos, int yPos, int mouseX, int mouseY, float partialTick) {
        int textColor = 16777215;
        guiGraphics.blit(TEXTURE, xPos, yPos, 0, 0, WIDTH, HEIGHT);
        var skillHandler = player.getData(DataInit.SKILL_HANDLER);

        for (int i = 0; i < 4; i++) {
            guiGraphics.blit(TEXTURE, xPos + 19, yPos + 27 + (i * 28), 110, 230, 73, 26);
            if (this.spellList.size() > i + scrollIndex) {
                var spellType = this.spellList.get(i + scrollIndex);
                if (spellType != null) {
                    if (isHovering(19, 25 + (i * 26), 73, 26, mouseX, mouseY) || i == selectedIndex) {
                        guiGraphics.blit(TEXTURE, xPos + 19, yPos + 27 + (i * 28), 183, 230, 73, 26);
                    } else {
                        guiGraphics.blit(TEXTURE, xPos + 19, yPos + 27 + (i * 28), 183, 204, 73, 26);
                    }
                    Component component = spellType.createSpell().getSpellName();
                    int height = component.getString().length() > 12 ? 35 : 40;
                    RenderUtil.drawWordWrap(guiGraphics, this.font, component, xPos + 55, yPos + height + (i * 28), 68, textColor);
                }
            }
        }

        int scrollTexture = isHovering(5, 29, 12, 104, mouseX, mouseY) ? 12 : 0;
        guiGraphics.drawCenteredString(this.font, this.selectedSpell != null ? this.selectedSpell.createSpell().getSpellName() : Component.empty(), xPos + 75, yPos + 148, textColor);
        guiGraphics.drawCenteredString(this.font, this.selectedSpell != null ? Component.literal(String.valueOf(skillHandler.getSpellLevel(selectedSpell))) : Component.empty(), xPos + 14, yPos + 149, textColor);
        guiGraphics.blit(TEXTURE, xPos + 5, (int) (yPos + 29 + (89 * scrollOffs)), scrollTexture, 172, 12, 15);
        guiGraphics.blit(PATH, xPos + 4, yPos + 4, 0, 1 + (SpellPath.values()[pageIndex].ordinal() * 20), 84, 18, 84, 100);

        renderScaledXPBars(guiGraphics, skillHandler, xPos, yPos, textColor);
        renderTabs(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private void renderWindow(GuiGraphics guiGraphics, int xPos, int yPos, int mouseX, int mouseY) {
        UpgradeWindow window = this.selectedTree;
        if (window == null) {
            guiGraphics.fill(xPos + 98, yPos + 16, xPos + 98 + 149, yPos + 16 + 115, -16777216);
        } else {
            window.drawContents(guiGraphics, xPos + 98, yPos + 16);
        }
    }

    private void renderTooltips(GuiGraphics guiGraphics, int xPos, int yPos, int mouseX, int mouseY) {
        if (this.selectedTree != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((float)(xPos + 98), (float)(yPos + 16), 400);
            RenderSystem.enableDepthTest();
            this.selectedTree.drawTooltips(guiGraphics, xPos, yPos, mouseX - xPos - 98, mouseY - yPos - 16);
            RenderSystem.disableDepthTest();
            guiGraphics.pose().popPose();
        }
    }

    private void renderTabs(GuiGraphics guiGraphics, int xPos, int yPos, int mouseX, int mouseY) {
        for (int i = 0; i < 5; i++) {
            guiGraphics.blit(TEXTURE, xPos + 92 + (i * 26), yPos - 26, 0, 198, 26, 26);
            if (isHovering(92 + (i * 26), -30, 26, 26, mouseX, mouseY) || i == pageIndex) {
                guiGraphics.blit(TEXTURE, xPos + 92 + (i * 26), yPos - 28, 0, 224, 26, 32);
            }
        }
    }

    private void renderScaledXPBars(GuiGraphics guiGraphics, SkillHandler skillHandler, int xPos, int yPos, int textColor) {
        float pathXP = skillHandler.getPathXp(SpellPath.values()[this.pageIndex]);
        int pathLevel = skillHandler.getPathLevel(SpellPath.values()[this.pageIndex]);
        if (pathXP > 0) {
            int xpGoal = 100 * (pathLevel + 1);
            int scale = RenderUtil.getScaledRender(pathXP - (100 * pathLevel), xpGoal - (100 * pathLevel), 141);
            guiGraphics.blit(TEXTURE, xPos + 101, yPos + 5, 115, 188, scale, 5);
        }

        if (this.selectedSpell == null) return;

        float spellXP = skillHandler.getSpellXp(this.selectedSpell);
        int spellLevel = skillHandler.getSpellLevel(this.selectedSpell);
        if (spellXP > 0) {
            int xpGoal = Math.min(100 * (spellLevel + 1), 500);
            int scale = RenderUtil.getScaledRender(spellXP - (100 * spellLevel), xpGoal - (100 * spellLevel), 122);
            guiGraphics.drawCenteredString(this.font, Component.literal((int )spellXP + " / " + xpGoal), xPos + 190, yPos + 143, textColor);
            guiGraphics.blit(TEXTURE, xPos + 129, yPos + 153, 134, 194, spellXP != 500 ? scale : 122, 5);
        }
    }

    private void initSpellTrees() {
        for (var root : this.upgradeTree.roots()) {
            UpgradeWindow window = new UpgradeWindow(this.minecraft, this, root.skill().getSpell(), root);
            this.spellTrees.put(root.skill().getSpell(), window);
        }

        for (var child : this.upgradeTree.children()) {
            UpgradeWindow window = this.getWindow(child);
            if (window != null) {
                window.addUpgrade(child);
            }
        }
    }

    @Nullable
    private UpgradeWindow getWindow(SkillNode skillNode) {
        return this.spellTrees.get(skillNode.skill().getSpell());
    }

    private boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        double d0 = pMouseX - (double)(i + pX);
        double d1 = pMouseY - (double)(j + pY);
        return d0 >= 0 && d1 >= 0 && d0 < pWidth && d1 < pHeight;
    }

    private float subtractInputFromScroll(float scrollOffs, double input) {
        return Mth.clamp(scrollOffs - (float)(input / this.spellList.size()), 0.0F, 1.0F);
    }

    private void scrollTo(float pos) {
        int k = Math.max(0, this.spellList.size() - 4);
        for (int l = 0; l <= k; l ++) {
            float thresh = (float) l / (k + 1);
            float nextThresh = (float) (l + 1) / (k + 1);
            if (pos > thresh && pos <= nextThresh)
                this.scrollIndex = l;
        }
    }

    private boolean canScroll() {
        return this.spellList.size() > 4;
    }
}
