package com.ombremoon.spellbound.client.gui.radial;

import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.RadialSpell;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * Adapted from https://github.com/gigaherz/ToolBelt under the following license:
 * <p>
 * Copyright (c) 2015, David Quintana <gigaherz@gmail.com>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the author nor the
 * names of the contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class SpellRadialMenuItem extends RadialMenuItem {
    private final SpellType<?> spellType;

    public SpellRadialMenuItem(RadialMenu owner, SpellType<?> spellType) {
        super(owner);
        this.spellType = spellType;
    }

    public SpellType<?> getSpellType() {
        return this.spellType;
    }

    @Override
    public boolean isSelected() {
        var handler = SpellUtil.getSpellHandler(this.owner.getScreen().getMinecraft().player);
        return handler.getSelectedSpell() == this.spellType;
    }

    @Override
    public void draw(DrawingContext context) {
        ResourceLocation sprite = this.getSpellType().createSpell().getTexture();
        float x = context.x - 12;
        float y = context.y - 12;
        context.guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
        context.guiGraphics.blit(sprite, (int) x, (int) y, 0, 0, 24, 24, 24, 24);
        context.guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void drawTooltips(DrawingContext context) {
        List<Component> list = List.of(this.getSpellType().createSpell().getName());
        context.guiGraphics.renderTooltip(context.fontRenderer, list, Optional.empty(), (int) context.x, (int) context.y);
    }

    @Override
    public boolean onClick() {
        AbstractSpell spell = this.spellType.createSpell();
        if (spell instanceof RadialSpell radialSpell) {
            //CHANGE TO JUST OPEN SKILL RADIAL MENU
            radialSpell.onClick();
            return true;
        }
        return super.onClick();
    }
}
