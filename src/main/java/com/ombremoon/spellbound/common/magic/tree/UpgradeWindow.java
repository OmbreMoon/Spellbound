package com.ombremoon.spellbound.common.magic.tree;

import com.ombremoon.spellbound.client.gui.WorkbenchScreen;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.client.Minecraft;

public class UpgradeWindow {
    private final Minecraft minecraft;
    private final WorkbenchScreen screen;
    private final SpellType<?> spellType;
    private final SkillNode root;
    private int page;

    public UpgradeWindow(Minecraft minecraft, WorkbenchScreen screen, SpellType<?> spellType, SkillNode root) {
        this.minecraft = minecraft;
        this.screen = screen;
        this.spellType = spellType;
        this.root = root;
    }

    public UpgradeWindow(Minecraft minecraft, WorkbenchScreen screen, SpellType<?> spellType, SkillNode root, int page) {
        this(minecraft, screen, spellType, root);
        this.page = page;
    }

    public int getPage() {
        return this.page;
    }


}
