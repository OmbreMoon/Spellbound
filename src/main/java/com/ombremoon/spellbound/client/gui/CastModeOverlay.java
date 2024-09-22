package com.ombremoon.spellbound.client.gui;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CastModeOverlay implements LayeredDraw.Layer {
    private static final ResourceLocation BACKGROUND = CommonClass.customLocation("textures/icons/spell_background.png");

    public CastModeOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
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

        ResourceLocation spellLoc = spell.getResourceLocation();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(spellLoc.getNamespace(), "textures/icons/" + spellLoc.getPath() + ".png");

        guiGraphics.blit(BACKGROUND, x-5, y-5, 0, 0, 34, 34, 34, 34);
        guiGraphics.blit(texture, x, y, 0, 0, 24, 24, 24, 24);

        guiGraphics.drawString(Minecraft.getInstance().font,
                player.getData(DataInit.MANA) + "/" + player.getData(DataInit.MAX_MANA),
                40,
                guiGraphics.guiHeight()-40,
                8889187 ,
                false);
    }
}
