package com.ombremoon.spellbound.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ombremoon.spellbound.Constants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinds {
    public static final String KEY_CATEGORY_SPELLBOUND = "key.category." + Constants.MOD_ID;
    public static final String KEY_SWITCH_MODE = "key." + Constants.MOD_ID + ".switch_mode";

    public static final KeyMapping SWITCH_MODE_BINDING = new KeyMapping(KEY_SWITCH_MODE, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY_SPELLBOUND);

    public static KeyMapping getSpellCastMapping() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.options.keyUse;
    }

    public static KeyMapping getCycleSpellMapping() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.options.keyShift;
    }
}
