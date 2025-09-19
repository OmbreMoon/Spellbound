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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record GuideItem(ResourceLocation itemLoc, float scale, ElementPosition position) implements PageElement {
    private static final ResourceLocation ITEM_TILE = CommonClass.customLocation("textures/gui/books/item_tile.png");

    public static final MapCodec<GuideItem> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(GuideItem::itemLoc),
            Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(GuideItem::scale),
            ElementPosition.CODEC.optionalFieldOf("position", ElementPosition.getDefault()).forGetter(GuideItem::position)
    ).apply(inst, GuideItem::new));

    @Override
    public void render(GuiGraphics graphics, int leftPos, int topPos, int mouseX, int mouseY, float partialTick) {
        Item item = Minecraft.getInstance().level.registryAccess().registry(Registries.ITEM).get().get(itemLoc);
        if (item == null) return;
        graphics.blit(ITEM_TILE, leftPos + position.xOffset(), topPos + position.yOffset(), 0, 0, (int) (48 * scale), (int) (46 * scale), (int) (48 * scale), (int) (46 * scale));
        renderItem(graphics, item.getDefaultInstance(), leftPos + position.xOffset(), topPos + position.yOffset(), 1.3f * scale);
    }

    public static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y, float scale) {
        if (!stack.isEmpty()) {
            Minecraft minecraft = Minecraft.getInstance();
            BakedModel bakedmodel = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate((x + (19*scale)), (y + (17*scale)), (float)(150));

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
