package com.ombremoon.spellbound.client.gui;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.init.SBAttributes;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;


public class CastModeOverlay implements LayeredDraw.Layer {
    private static final ResourceLocation BACKGROUND = CommonClass.customLocation("textures/gui/spells/spell_background.png");

    public CastModeOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        render(guiGraphics, deltaTracker);
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;
        SpellHandler handler = SpellUtil.getSpellHandler(player);
        if (!handler.inCastMode()) return;

        int x = guiGraphics.guiWidth() - 40;
        int y = 16;
        SpellType<?> spell = handler.getSelectedSpell();
        if (spell == null) return;

        ResourceLocation texture = spell.createSpell().getTexture();

        guiGraphics.blit(texture, x, y, 0, 0, 24, 24, 24, 24);
        guiGraphics.blit(BACKGROUND, x-1, y-1, 0, 0, 26, 26, 26, 26);
        guiGraphics.drawString(Minecraft.getInstance().font,
                spell.createSpell().getName(),
                40, guiGraphics.guiHeight()-60,
                8889187, false);

        guiGraphics.drawString(Minecraft.getInstance().font,
                player.getData(SBData.MANA) + "/" + player.getAttribute(SBAttributes.MAX_MANA).getValue(),
                40,
                guiGraphics.guiHeight()-40,
                8889187 ,
                false);
    }
}
