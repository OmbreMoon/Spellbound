package com.ombremoon.spellbound.client.gui;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.tree.SkillNode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class UpgradeWidget {
    private final UpgradeWindow window;
    private final Minecraft minecraft;
    private final SkillNode skillNode;
    private final List<UpgradeWidget> parents = new ObjectArrayList<>();
    private final List<UpgradeWidget> children = new ObjectArrayList<>();
    private final int x;
    private final int y;

    public UpgradeWidget(UpgradeWindow window, Minecraft minecraft, SkillNode skillNode) {
        this.window = window;
        this.minecraft = minecraft;
        this.skillNode = skillNode;
        this.x = skillNode.skill().getX();
        this.y = -skillNode.skill().getY();
    }

    public Skill getSkill() {
        return this.skillNode.skill();
    }

    public void draw(GuiGraphics guiGraphics, int x, int y) {
        var handler = this.minecraft.player.getData(DataInit.SKILL_HANDLER);
        Type type = handler.hasSkill(this.skillNode.skill()) ? Type.UNLOCKED : Type.LOCKED;
        guiGraphics.blit(WorkbenchScreen.TEXTURE, x + this.x, y + this.y, 29 ,226, 30, 30);
        ResourceLocation sprite = this.skillNode.skill().getSkillTexture();
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
//        Constants.LOG.info("Hell yea!");
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
