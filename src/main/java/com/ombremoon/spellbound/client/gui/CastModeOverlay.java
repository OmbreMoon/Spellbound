package com.ombremoon.spellbound.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.init.SBAttributes;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;


public class CastModeOverlay implements LayeredDraw.Layer {
    private static final ResourceLocation BACKGROUND = CommonClass.customLocation("textures/gui/spells/spell_background.png");

    public CastModeOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        render(guiGraphics, deltaTracker);
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;
        var handler = SpellUtil.getSpellHandler(player);
        if (handler.inCastMode())
            renderCastMode(guiGraphics, player, handler);

        Set<AbstractSpell> spells = new ObjectOpenHashSet<>(handler.getActiveSpells());
        RenderSystem.enableBlend();
        int j1 = 0;
        int k1 = 0;
        List<Runnable> list = Lists.newArrayListWithExpectedSize(spells.size());

        for (AbstractSpell spell : spells) {
            //Add per spell config to disable rendering
            int i = 1;
            int j = 1;

            j1++;
            i += 27 * j1;
            float f = 1.0F;
            guiGraphics.blit(BACKGROUND, i, j, 0, 0, 26, 26, 26, 26);
            if (spell.getDuration() - spell.ticks < 200 && spell.getCastType() != AbstractSpell.CastType.CHANNEL) {
                int k = spell.ticks;
                int l = 10 - k / 20;
                f = Mth.clamp((float)k / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                        + Mth.cos((float)k * (float) Math.PI / 5.0F) * Mth.clamp((float)l / 10.0F * 0.25F, 0.0F, 0.25F);
            }

            int l1 = i + 1;
            int i1 = j + 1;
            float f1 = f;
            list.add(() -> {
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, f1);
                guiGraphics.blit(spell.getTexture(), l1, i1, 0, 0, 24, 24, 24, 24);
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            });
        }

        list.forEach(Runnable::run);
        RenderSystem.disableBlend();
    }

    private void renderCastMode(GuiGraphics guiGraphics, Player player, SpellHandler handler) {

        int x = guiGraphics.guiWidth() - 27;
        int y = 30;
        SpellType<?> spell = handler.getSelectedSpell();
        if (spell == null) return;

        ResourceLocation texture = spell.createSpell().getTexture();

        guiGraphics.blit(texture, x, y, 0, 0, 24, 24, 24, 24);
        guiGraphics.blit(BACKGROUND, x - 1, y - 1, 0, 0, 26, 26, 26, 26);
        guiGraphics.drawString(Minecraft.getInstance().font,
                spell.createSpell().getName(),
                40, guiGraphics.guiHeight() - 60,
                8889187, false);

        guiGraphics.drawString(Minecraft.getInstance().font,
                player.getData(SBData.MANA) + "/" + player.getAttribute(SBAttributes.MAX_MANA).getValue(),
                40,
                guiGraphics.guiHeight() - 40,
                8889187 ,
                false);
    }
}
