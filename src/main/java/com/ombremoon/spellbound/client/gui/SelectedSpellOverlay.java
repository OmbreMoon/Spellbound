package com.ombremoon.spellbound.client.gui;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

public class SelectedSpellOverlay implements LayeredDraw.Layer {
    private static final ResourceLocation BACKGROUND = CommonClass.customLocation("textures/gui/spells/spell_background.png");

    public SelectedSpellOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        render(guiGraphics, deltaTracker);
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;
        SpellHandler handler = player.getData(DataInit.SPELL_HANDLER);
        if (!handler.inCastMode()) return;

        int x = guiGraphics.guiWidth() - 40;
        int y = 16;
        SpellType<?> spell = handler.getSelectedSpell();
        if (spell == null) return;

        ResourceLocation texture = spell.createSpell().getSpellTexture();

        guiGraphics.blit(BACKGROUND, x-5, y-5, 0, 0, 34, 34, 34, 34);
        guiGraphics.blit(texture, x, y, 0, 0, 24, 24, 24, 24);
    }
}
