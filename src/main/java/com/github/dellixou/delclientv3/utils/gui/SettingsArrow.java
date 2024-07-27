package com.github.dellixou.delclientv3.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class SettingsArrow {

    private float currentRotation2 = 0.0f;
    private float currentScale = 1.0f;  // Variable to track the current scale

    public void drawComboArrow(float x, float y, float width, float height, ResourceLocation image, boolean isExpanded, float partialTicks, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);

        GlStateManager.pushMatrix();

        // Determine target rotation based on expanded state
        float targetRotation = isExpanded ? 180.0f : 0.0f;

        // Interpolate rotation using partialTicks
        float interpolatedRotation = currentRotation2 + (targetRotation - currentRotation2) * partialTicks;

        // Determine target scale based on hover state
        float targetScale = isExpanded ? 1f : 0.9f;

        // Interpolate scale using partialTicks
        float interpolatedScale = currentScale + (targetScale - currentScale) * partialTicks;

        // Translate to the center of the image before rotating and scaling
        GlStateManager.translate(x + width / 2, y + height / 2, 0);
        GlStateManager.rotate(interpolatedRotation, 0, 0, 1);
        GlStateManager.scale(interpolatedScale, interpolatedScale, interpolatedScale);
        GlStateManager.translate(-(x + width / 2), -(y + height / 2), 0);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0).tex(0, 1).endVertex();
        worldrenderer.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        worldrenderer.pos(x + width, y, 0).tex(1, 0).endVertex();
        worldrenderer.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();

        // Update currentRotation and currentScale for next frame
        currentRotation2 = interpolatedRotation;
        currentScale = interpolatedScale;

        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

}
