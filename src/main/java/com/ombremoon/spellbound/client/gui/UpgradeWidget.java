package com.ombremoon.spellbound.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.tree.SkillNode;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class UpgradeWidget {
    private static final ResourceLocation TITLE_BOX_SPRITE = ResourceLocation.withDefaultNamespace("advancements/title_box");
    private static final ResourceLocation LOCKED = ResourceLocation.withDefaultNamespace("advancements/box_unobtained");
    private static final ResourceLocation UNLOCKED = ResourceLocation.withDefaultNamespace("advancements/box_obtained");
    private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
    private final UpgradeWindow window;
    private final Minecraft minecraft;
    private final SkillNode skillNode;
    private final FormattedCharSequence title;
    private final int width;
    private final List<FormattedCharSequence> description;
    private final List<UpgradeWidget> parents = new ObjectArrayList<>();
    private final List<UpgradeWidget> children = new ObjectArrayList<>();
    private final int x;
    private final int y;

    public UpgradeWidget(UpgradeWindow window, Minecraft minecraft, SkillNode skillNode) {
        this.window = window;
        this.minecraft = minecraft;
        this.skillNode = skillNode;
        this.title = Language.getInstance().getVisualOrder(minecraft.font.substrByWidth(skillNode.skill().getName(), 163));
        this.x = skillNode.skill().getX();
        this.y = -skillNode.skill().getY();
        int j = 29 + minecraft.font.width(this.title);
        this.description = Language.getInstance()
                .getVisualOrder(
                        this.findOptimalLines(ComponentUtils.mergeStyles(skillNode.skill().getDescription(), Style.EMPTY.withColor(getSkill().getSpell().getPath().getColor())), j)
                );

        for (FormattedCharSequence formattedcharsequence : this.description) {
            j = Math.max(j, minecraft.font.width(formattedcharsequence));
        }

        this.width = j + 3 + 5;
    }

    public Skill getSkill() {
        return this.skillNode.skill();
    }

    private static float getMaxWidth(StringSplitter manager, List<FormattedText> text) {
        return (float)text.stream().mapToDouble(manager::stringWidth).max().orElse(0.0);
    }

    private List<FormattedText> findOptimalLines(Component component, int maxWidth) {
        StringSplitter stringsplitter = this.minecraft.font.getSplitter();
        List<FormattedText> list = null;
        float f = Float.MAX_VALUE;

        for (int i : TEST_SPLIT_OFFSETS) {
            List<FormattedText> list1 = stringsplitter.splitLines(component, maxWidth - i, Style.EMPTY);
            float f1 = Math.abs(getMaxWidth(stringsplitter, list1) - (float)maxWidth);
            if (f1 <= 10.0F) {
                return list1;
            }

            if (f1 < f) {
                f = f1;
                list = list1;
            }
        }

        return list;
    }

    public void drawConnection(GuiGraphics guiGraphics, int x, int y, boolean dropShadow) {
        if (!this.parents.isEmpty()) {
            for (var parent : parents) {
                int i = y + this.y + 30 + 4;
                int j = x + parent.x + 15;
                int k = x + this.x + 15;
                int l = y + this.y + 15;
                int m = y + parent.y + 15;
                int n = dropShadow ? -16777216 : -1;
                if (dropShadow) {
                    guiGraphics.hLine(k - 1, j, i - 1, n);
                    guiGraphics.hLine(k + 1, j, i + 1, n);
                    guiGraphics.hLine(k - 1, j, i + 1, n);
                    guiGraphics.vLine(j - 1, i - 2, m, n);
                    guiGraphics.vLine(j + 1, i - 2, m, n);
                    guiGraphics.vLine(k, i + 1, l - 1, n);
                    guiGraphics.vLine(k - 1, i + 1, l, n);
                    guiGraphics.vLine(k + 1, i + 1, l, n);
                } else {
                    guiGraphics.vLine(k, i, l, n);
                    guiGraphics.vLine(j, i, m, n);
                    guiGraphics.hLine(k, j, i, n);
                }
            }
        }

        for (var widget : this.children) {
            widget.drawConnection(guiGraphics, x, y, dropShadow);
        }
    }

    public void draw(GuiGraphics guiGraphics, int x, int y) {
        var holder = SpellUtil.getSkillHolder(this.minecraft.player);
        Type type = holder.hasSkill(this.skillNode.skill()) ? Type.UNLOCKED : Type.LOCKED;
        guiGraphics.blit(WorkbenchScreen.TEXTURE, x + this.x, y + this.y, 39 ,226, 30, 30);
        ResourceLocation sprite = this.skillNode.skill().getTexture();
        guiGraphics.blit(sprite, x + this.x + 3, y + this.y + 3, 0, 0, 24, 24, 24, 24);

        for (var widget : this.children) {
            widget.draw(guiGraphics, x, y);
        }
    }

    public boolean isMouseOver(int xPos, int yPos, double mouseX, double mouseY) {
        int i = xPos + this.x;
        int j = i + 30;
        int k = yPos + this.y;
        int l = k + 30;
        return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
    }

    private void addChild(UpgradeWidget widget) {
        this.children.add(widget);
    }

    public void drawHover(GuiGraphics guiGraphics, int x, int y, float fade, int width, int height) {
        boolean flag = width + x + this.x + this.width + 30 >= this.window.getScreen().width;
        boolean flag1 = 115 - y - this.y - 30 <= 6 + this.description.size() * 9;
        var holder = SpellUtil.getSkillHolder(this.minecraft.player);
        ResourceLocation box = holder.hasSkill(getSkill()) ? UNLOCKED : LOCKED;

        int i = this.width;
        RenderSystem.enableBlend();
        int j = y + this.y;
        int k;
        if (flag) {
            k = x + this.x - this.width + 26 + 6;
        } else {
            k = x + this.x;
        }

        int l = 37 + this.description.size() * 9;
        if (!this.description.isEmpty()) {
            if (flag1) {
                guiGraphics.blitSprite(TITLE_BOX_SPRITE, k, j + 26 - l, this.width, l);
            } else {
                guiGraphics.blitSprite(TITLE_BOX_SPRITE, k, j, this.width, l);
            }
        }

        guiGraphics.blitSprite(box, 200, 26, 200 - i, 0, k, j, i, 26);
        guiGraphics.blit(WorkbenchScreen.TEXTURE, x + this.x, y + this.y, 39 ,226, 30, 30);
        if (flag) {
            guiGraphics.drawString(this.minecraft.font, this.title, k + 5, y + this.y + 9, -1);
        } else {
            guiGraphics.drawString(this.minecraft.font, this.title, x + this.x + 32, y + this.y + 9, -1);
        }

        if (flag1) {
            for (int i1 = 0; i1 < this.description.size(); i1++) {
                guiGraphics.drawString(this.minecraft.font, this.description.get(i1), k + 5, j + 26 - l + 9 + i1 * 9, -5592406, false);
            }
        } else {
            for (int j1 = 0; j1 < this.description.size(); j1++) {
                guiGraphics.drawString(this.minecraft.font, this.description.get(j1), k + 5, y + this.y + 9 + 22 + j1 * 9, -5992406, false);
            }
        }

        ResourceLocation sprite = this.skillNode.skill().getTexture();
        guiGraphics.blit(sprite, x + this.x + 3, y + this.y + 3, 0, 0, 24, 24, 24, 24);
    }

    public void attachToParents() {
        if (this.parents.isEmpty() && !this.skillNode.parents().isEmpty()) {
            this.skillNode.parents().forEach(skill -> this.parents.add(this.window.getWidget(skill.skill())));
            for (var node : this.skillNode.parents()) {
                if (node != null) this.parents.forEach(widget -> widget.addChild(this));
            }
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public enum Type {
        UNLOCKED,
        LOCKED
    }
}
