package com.github.dellixou.delclientv3.gui.clickgui.util;

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

    private float currentRotation = 0;

    public void drawModArrow(float x, float y, float width, float height, ResourceLocation image, boolean isExpanded, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.75f); // Set color with 75% transparency
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);

        // Save current matrix
        GL11.glPushMatrix();

        // Determine target rotation based on expanded state
        float targetRotation = isExpanded ? 90.0f : 0.0f;

        // Interpolate rotation using partialTicks
        float interpolatedRotation = currentRotation + (targetRotation - currentRotation) * partialTicks;

        // Move to the center of the image before rotating
        GL11.glTranslatef(x + width / 2, y + height / 2, 0);

        // Rotate based on interpolated rotation
        GL11.glRotatef(interpolatedRotation, 0, 0, 1);

        // Move back after rotating
        GL11.glTranslatef(-(x + width / 2), -(y + height / 2), 0);

        // Draw the image
        Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, 0.0f, 0.0f, (int) width, (int) height, width, height);

        // Update currentRotation for next frame
        currentRotation = interpolatedRotation;

        // Restore original matrix
        GL11.glPopMatrix();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private float currentRotation2 = 0.0f;
    private float currentScale = 1.0f;  // Variable to track the current scale

    public void drawComboArrow(float x, float y, float width, float height, ResourceLocation image, boolean isExpanded, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

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
