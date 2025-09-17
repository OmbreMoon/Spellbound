package com.ombremoon.spellbound.common.magic.acquisition.guides.elements;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.magic.acquisition.guides.elements.extras.ElementPosition;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record GuideRecipe(ResourceLocation recipeLoc, float scale, ElementPosition position) implements PageElement {
    private static final ResourceLocation LARGE_GRID = CommonClass.customLocation("textures/gui/books/large_crafting_grid.png");
    private static final ResourceLocation SMALL_GRID = CommonClass.customLocation("textures/gui/books/small_crafting_grid.png");

    public static final MapCodec<GuideRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("recipe").forGetter(GuideRecipe::recipeLoc),
            Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(GuideRecipe::scale),
            ElementPosition.CODEC.fieldOf("position").forGetter(GuideRecipe::position)
    ).apply(inst, GuideRecipe::new));

    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick) {
        RecipeManager manager = Minecraft.getInstance().player.connection.getRecipeManager();
        Optional<RecipeHolder<?>> recipeOpt = manager.byKey(recipeLoc);
        if (recipeOpt.isEmpty()) return;

        RecipeHolder<?> recipeHolder = recipeOpt.get();
        Recipe<?> recipe = recipeHolder.value();
        if (recipe.getType() != RecipeType.CRAFTING) return;

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            GhostRecipe ghostRecipe = new GhostRecipe();
            ghostRecipe.setRecipe(recipeHolder);

            boolean largeGrid = shapedRecipe.getWidth() > 2 || shapedRecipe.getHeight() > 2;
            if (largeGrid) {
                graphics.blit(LARGE_GRID, leftPos + position.xOffset(), topPos + position.yOffset(), 0, 0, (int) (81F * scale), (int) (82F * scale), (int) (81 * scale), (int) (82 * scale));

                for (int i = 1; i <= 9; i++) {
                    int slotXOffset = (i-1)%3 * (int) ( 23 * scale);
                    int slotYOffset = 1;
                    if (i > 3) slotYOffset += (int) (23 * scale);
                    if (i > 6) slotYOffset += (int) (23 * scale);

                    ghostRecipe.addIngredient(recipe.getIngredients().get(i-1), slotXOffset, slotYOffset);
                }
            }
            else {
                graphics.blit(SMALL_GRID, leftPos + position.xOffset(), topPos + position.yOffset(), 0, 0, (int) (60 * scale), (int) (58 * scale), (int) (60 * scale), (int) (58 * scale));

                for (int i = 1; i <= 4; i++) {
                    int slotXOffset = (i+1)%2 * ( (int) (23 * scale));
                    int slotYOffset = i > 2 ? ( int)(23 * scale) : 0;

                    ghostRecipe.addIngredient(recipe.getIngredients().get(i-1), slotXOffset, slotYOffset);
                }
            }

            renderRecipe(graphics, leftPos + position.xOffset(), topPos + position.yOffset(), ghostRecipe);
        }
    }

    public void renderRecipe(GuiGraphics guiGraphics, int leftPos, int topPos, GhostRecipe recipe) {

        for(int i = 0; i < recipe.size(); ++i) {
            GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = recipe.get(i);
            int j = ghostrecipe$ghostingredient.getX() + leftPos;
            int k = ghostrecipe$ghostingredient.getY() + topPos;

            ItemStack itemstack = ghostrecipe$ghostingredient.getItem();
            renderItem(guiGraphics, itemstack, j, k, scale);
        }

    }

    public static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y, float scale) {
        if (!stack.isEmpty()) {
            Minecraft minecraft = Minecraft.getInstance();
            BakedModel bakedmodel = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate((x + (18*scale)), (y + (16*scale)), (float)(150));

            try {
                float size = 16.0F * 1.2F * scale;
                pose.scale(size, -size, size);
                boolean flag = !bakedmodel.usesBlockLight();
                if (flag) {
                    Lighting.setupForFlatItems();
                }

                minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose, graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
                graphics.flush();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashreport);
            }

            pose.popPose();
        }
    }

    @Override
    public @NotNull MapCodec<? extends PageElement> codec() {
        return CODEC;
    }
}
