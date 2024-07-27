package com.github.dellixou.delclientv3.utils;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;

import static com.github.dellixou.delclientv3.utils.DrawUtils.*;
import static org.lwjgl.opengl.GL11.*;


public class RenderUtils {

    private static Minecraft mc = Minecraft.getMinecraft();
    private static GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 40, true, true, true);

    public static void renderBoxOutline(double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        // Get partial ticks

        // Calculate the position offset from the player's point of view
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * 20;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * 20;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * 20;

        // Set up the box's coordinates
        AxisAlignedBB boundingBox = new AxisAlignedBB(x - 0.5 - playerX, y - playerY, z - 0.5 - playerZ, x + 0.5 - playerX, y + 1 - playerY, z + 0.5 - playerZ);

        // Prepare for rendering
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.depthMask(false);

        // Set color (RGBA)
        GlStateManager.color(1.0f, 0.0f, 0.0f, 1.0f); // Red color with full opacity

        // Start drawing lines
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        // Bottom face
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();

        // Top face
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();

        // Vertical lines
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();

        tessellator.draw();

        // Reset rendering state
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void highlightBlockOld(BlockPos pos, Color color, float opacity, float partialTicks) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double x = pos.getX() - (viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks);
        double y = pos.getY() - (viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks);
        double z = pos.getZ() - (viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks);
        //GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        drawFilledBoundingBox(new AxisAlignedBB(x-0.01f, y+0.01f, z-0.01f, x + 1.01f, y + 1.01f, z + 1.01f), color, opacity);
        //GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    public static void drawFilledBoundingBox(AxisAlignedBB aabb, Color c, float alphaMultiplier) {
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f,
                c.getAlpha() / 255f * alphaMultiplier);
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        tessellator.draw();
        GlStateManager.color(c.getRed() / 255f * 0.8f, c.getGreen() / 255f * 0.8f, c.getBlue() / 255f * 0.8f,
                c.getAlpha() / 255f * alphaMultiplier);
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        GlStateManager.color(c.getRed() / 255f * 0.9f, c.getGreen() / 255f * 0.9f, c.getBlue() / 255f * 0.9f,
                c.getAlpha() / 255f * alphaMultiplier);
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawOutlinedBoundingBox(BlockPos pos, float red, float green, float blue, float alpha) {
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_LINES);

        // Draw bottom face
        GL11.glVertex3d(pos.getX(), pos.getY(), pos.getZ());
        GL11.glVertex3d(pos.getX() + 1, pos.getY(), pos.getZ());
        GL11.glVertex3d(pos.getX() + 1, pos.getY(), pos.getZ());
        GL11.glVertex3d(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
        GL11.glVertex3d(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
        GL11.glVertex3d(pos.getX(), pos.getY(), pos.getZ() + 1);
        GL11.glVertex3d(pos.getX(), pos.getY(), pos.getZ() + 1);
        GL11.glVertex3d(pos.getX(), pos.getY(), pos.getZ());

        // Draw top face
        GL11.glVertex3d(pos.getX(), pos.getY() + 1, pos.getZ());
        GL11.glVertex3d(pos.getX() + 1, pos.getY() + 1, pos.getZ());
        GL11.glVertex3d(pos.getX() + 1, pos.getY() + 1, pos.getZ());
        GL11.glVertex3d(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        GL11.glVertex3d(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        GL11.glVertex3d(pos.getX(), pos.getY() + 1, pos.getZ() + 1);
        GL11.glVertex3d(pos.getX(), pos.getY() + 1, pos.getZ() + 1);
        GL11.glVertex3d(pos.getX(), pos.getY() + 1, pos.getZ());

        // Draw vertical lines
        GL11.glVertex3d(pos.getX(), pos.getY(), pos.getZ());
        GL11.glVertex3d(pos.getX(), pos.getY() + 1, pos.getZ());
        GL11.glVertex3d(pos.getX() + 1, pos.getY(), pos.getZ());
        GL11.glVertex3d(pos.getX() + 1, pos.getY() + 1, pos.getZ());
        GL11.glVertex3d(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
        GL11.glVertex3d(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        GL11.glVertex3d(pos.getX(), pos.getY(), pos.getZ() + 1);
        GL11.glVertex3d(pos.getX(), pos.getY() + 1, pos.getZ() + 1);

        GL11.glEnd();
    }

    private static void drawBlockOutline(BlockPos pos, float red, float green, float blue, float alpha) {
        double x = pos.getX() - mc.getRenderManager().viewerPosX;
        double y = pos.getY() - mc.getRenderManager().viewerPosY;
        double z = pos.getZ() - mc.getRenderManager().viewerPosZ;

        AxisAlignedBB box = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(red, green, blue, alpha);
        GL11.glLineWidth(0.5F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL13.GL_MULTISAMPLE);

        RenderHelper.disableStandardItemLighting();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();

        worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();

        worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();

        tessellator.draw();

        GL11.glDisable(GL13.GL_MULTISAMPLE);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    public static void drawLineWithGL(Vec3 blockA, Vec3 blockB, boolean whiteLine, float red, float green, float blue) {

        double width = 2f;

        if (whiteLine) {
            GL11.glColor4f(255, 255, 255, 0F);
            GL11.glLineWidth(3);
        } else {
            GL11.glLineWidth((float) width);
            GL11.glColor4f(red, green, blue, 0F);
        }

        GL11.glBegin(GL11.GL_LINE_STRIP);

        GL11.glVertex3d(blockA.xCoord, blockA.yCoord, blockA.zCoord);
        GL11.glVertex3d(blockB.xCoord, blockB.yCoord, blockB.zCoord);

        GL11.glEnd();
    }

    public static void renderTextInWorld(Minecraft mc, String text, double x, double y, double z, float partialTicks, float scale, boolean bg) {
        Vec3 playerPos = mc.thePlayer.getPositionEyes(partialTicks);
        double distance = playerPos.distanceTo(new Vec3(x, y, z));
        float maxDistance = 20.0f;
        float minDistance = 8.0f;

        // Calculate opacity
        float opacity = 1.0f;

        // If the distance is greater than maxDistance, do not render the text
        if (distance >= maxDistance - 1) {
            opacity = 0.001f;
            return;
        }

        if (distance > minDistance) {
            opacity = 1.001f - (float) (distance - minDistance) / (maxDistance - minDistance);
        }


        // Ensure opacity is within the correct bounds
        opacity = Math.max(0.0f, Math.min(1.0f, opacity));

        boolean renderWall = DelClient.settingsManager.getSettingById("user_route_render_wall").getValBoolean();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - playerPos.xCoord, y - playerPos.yCoord, z - playerPos.zCoord);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        if (renderWall) {
            GL11.glDisable(GL11.GL_DEPTH_TEST); // Draw the text on top of the geometry
        }
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        RenderHelper.disableStandardItemLighting();

        // Calculate the color with alpha transparency
        int alpha = (int) (opacity * 255.0f);
        int color = (alpha << 24) | 0xFFFFFF; // Add the opacity to the color code

        // Draw the background rectangle
        //int textWidth = mc.fontRendererObj.getStringWidth(text);
        //int textHeight = mc.fontRendererObj.FONT_HEIGHT;
        //DrawingUtils.drawRect(-textWidth/2  - 3, 2, textWidth + 6, textHeight*2+5, new Color(47, 47, 47, 70));
        //mc.fontRendererObj.drawString(text, -textWidth / 2, 0, color);

        // Render the text
        int glyphWidth = glyphPageFontRenderer.getStringWidth(text);
        int glyphHeight = glyphPageFontRenderer.getFontHeight();

        // Define margin
        int margin = 4;

        // Draw the background rectangle with margin + text
        if (bg) {
            Gui.drawRect(-glyphWidth / 2 - margin, -glyphHeight / 2 - margin / 2, glyphWidth / 2 + margin, glyphHeight / 2 + margin / 2, new Color(47, 47, 47, 70).getRGB());
            glyphPageFontRenderer.drawString(text, -glyphWidth / 2, -glyphHeight / 2, color, true);
        } else {
            glyphPageFontRenderer.drawString(text, -glyphWidth / 2, -glyphHeight / 2, color, false);
        }


        //glyphPageFontRenderer.drawString(text, -glyphWidth/2, 0, -1, true);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static Color rainbow(int delay) {
        float v1 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_1").getValDouble();
        float v2 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_2").getValDouble();

        double rainbowstate = Math.ceil((System.currentTimeMillis() + delay) / 20);
        rainbowstate %= 360;
        return Color.getHSBColor((float) (rainbowstate / 360), v1 / 10, v2 / 10);
    }

    public static void scissor(float x, float y, float width, float height) {
        ScaledResolution sr = new ScaledResolution(mc);
        int scaleFactor = sr.getScaleFactor();

        GL11.glScissor((int) (x * scaleFactor),
                (int) (mc.displayHeight - (y + height) * scaleFactor),
                (int) (width * scaleFactor),
                (int) (height * scaleFactor));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public static void endScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static void drawFilledBoundingBox(AxisAlignedBB box) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);

        // Face avant
        worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();

        // Face arrière
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();

        tessellator.draw();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);

        // Face gauche
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();

        // Face droite
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();

        tessellator.draw();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);

        // Face supérieure
        worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();

        tessellator.draw();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);

        // Face inférieure
        worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();

        tessellator.draw();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);

        // Face latérale gauche
        worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();

        // Face latérale droite
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();

        tessellator.draw();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);

        // Face avant (bis)
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.minZ).endVertex();

        // Face arrière (bis)
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();

        tessellator.draw();
    }

    public static void drawOverlay(float lineWidth, boolean outline, boolean chroma, float chromaSpeed, float alpha, float red, float green, float blue) {
        if (Minecraft.getMinecraft().objectMouseOver != null &&
                Minecraft.getMinecraft().objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK)) {
            MovingObjectPosition position = Minecraft.getMinecraft().thePlayer.rayTrace(6.0D, 0.0F);
            if (position != null && position.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK)) {
                Block block = Minecraft.getMinecraft().thePlayer.worldObj.getBlockState(position.getBlockPos()).getBlock();
                if (block != null && !block.equals(Blocks.air) && !block.equals(Blocks.barrier) &&
                        !block.equals(Blocks.water) && !block.equals(Blocks.flowing_water) &&
                        !block.equals(Blocks.lava) && !block.equals(Blocks.flowing_lava)) {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    GL11.glLineWidth(lineWidth);
                    AxisAlignedBB box = block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, position.getBlockPos())
                            .expand(0.0010000000474974513D, 0.0010000000474974513D, 0.0010000000474974513D)
                            .offset(-Minecraft.getMinecraft().getRenderManager().viewerPosX,
                                    -Minecraft.getMinecraft().getRenderManager().viewerPosY,
                                    -Minecraft.getMinecraft().getRenderManager().viewerPosZ);

                    double millis;
                    Color color;
                    if (outline) {
                        if (chroma) {
                            millis = (double)((float)(System.currentTimeMillis() % (10000L / (long)chromaSpeed)) / (10000.0F / (float)chromaSpeed));
                            color = Color.getHSBColor((float)millis, 0.8F, 0.8F);
                            GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha);
                        } else {
                            GL11.glColor4f(red, green, blue, alpha);
                        }

                        RenderGlobal.drawSelectionBoundingBox(box);
                    } else if (chroma) {
                        millis = (double)((float)(System.currentTimeMillis() % (10000L / (long)chromaSpeed)) / (10000.0F / (float)chromaSpeed));
                        color = Color.getHSBColor((float)millis, 0.8F, 0.8F);
                        GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F);
                        RenderGlobal.drawSelectionBoundingBox(box);
                        GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha);
                        drawFilledBoundingBox(box);
                    } else {
                        GL11.glColor4f(red, green, blue, 1.0F);
                        RenderGlobal.drawSelectionBoundingBox(box);
                        GL11.glColor4f(red, green, blue, alpha);
                        drawFilledBoundingBox(box);
                    }

                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.depthMask(true);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    public static void highlightBlock(BlockPos pos, float lineWidth, boolean outline, boolean chroma, float chromaSpeed, float alpha, float red, float green, float blue) {
        if (pos != null) {
            Block block = Minecraft.getMinecraft().thePlayer.worldObj.getBlockState(pos).getBlock();
            if (block != null && !block.equals(Blocks.air) && !block.equals(Blocks.barrier) &&
                    !block.equals(Blocks.water) && !block.equals(Blocks.flowing_water) &&
                    !block.equals(Blocks.lava) && !block.equals(Blocks.flowing_lava)) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GL11.glLineWidth(lineWidth);
                AxisAlignedBB box = block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, pos)
                        .expand(0.0010000000474974513D, 0.0010000000474974513D, 0.0010000000474974513D)
                        .offset(-Minecraft.getMinecraft().getRenderManager().viewerPosX,
                                -Minecraft.getMinecraft().getRenderManager().viewerPosY,
                                -Minecraft.getMinecraft().getRenderManager().viewerPosZ);
                double millis;
                Color color;
                if (outline) {
                    if (chroma) {
                        millis = (double)((float)(System.currentTimeMillis() % (10000L / (long)chromaSpeed)) / (10000.0F / (float)chromaSpeed));
                        color = Color.getHSBColor((float)millis, 0.8F, 0.8F);
                        GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha);
                    } else {
                        GL11.glColor4f(red, green, blue, alpha);
                    }
                    RenderGlobal.drawSelectionBoundingBox(box);
                } else if (chroma) {
                    millis = (double)((float)(System.currentTimeMillis() % (10000L / (long)chromaSpeed)) / (10000.0F / (float)chromaSpeed));
                    color = Color.getHSBColor((float)millis, 0.8F, 0.8F);
                    GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F);
                    RenderGlobal.drawSelectionBoundingBox(box);
                    GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha);
                    drawFilledBoundingBox(box);
                } else {
                    GL11.glColor4f(red, green, blue, 1.0F);
                    RenderGlobal.drawSelectionBoundingBox(box);
                    GL11.glColor4f(red, green, blue, alpha);
                    drawFilledBoundingBox(box);
                }
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.depthMask(true);
                GlStateManager.popMatrix();
            }
        }
    }

    public static void renderIcon(ResourceLocation icon, int size, Color color, int x, int y){
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(icon);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, size, size, size, size);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void tracerLineToEntity(Entity entity, float partialTicks, float lineWidth, Color color) {
        // Get the Minecraft instance
        Minecraft mc = Minecraft.getMinecraft();

        // Calculate interpolated position of the entity
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks + (entity.height / 2.0F) - mc.getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        // Setup OpenGL for rendering
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(lineWidth);
        GL11.glDepthMask(false);
        setColor(color);

        // Start drawing the line
        GL11.glBegin(GL11.GL_LINES);

        // Draw line from player's eye position to the entity's position
        GL11.glVertex3d(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
        GL11.glVertex3d(x, y, z);

        // End drawing the line
        GL11.glEnd();

        // Restore OpenGL state
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void tracerLineToBlock(BlockPos blockPos, float partialTicks, float lineWidth, Color color) {

        if(mc.theWorld.getBlockState(blockPos).getBlock().getMaterial() == Material.air) return;

        double x = blockPos.getX() - mc.getRenderManager().viewerPosX + 0.5;
        double y = blockPos.getY() - mc.getRenderManager().viewerPosY + 0.5;
        double z = blockPos.getZ() - mc.getRenderManager().viewerPosZ + 0.5;

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(lineWidth);
        GL11.glDepthMask(false);
        setColor(color);

        GL11.glBegin(GL11.GL_LINES);

        GL11.glVertex3d(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
        GL11.glVertex3d(x, y, z);

        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void setColor(Color color) {
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }

    public static void drawBlockBox(final BlockPos blockPos, final Color color, final int width, float partialTicks) {
        if (width == 0)
            return;
        final RenderManager renderManager = mc.getRenderManager();

        final double x = blockPos.getX() - renderManager.viewerPosX;
        final double y = blockPos.getY() - renderManager.viewerPosY;
        final double z = blockPos.getZ() - renderManager.viewerPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final Block block = mc.theWorld.getBlockState(blockPos).getBlock();

        if (block != null) {
            final EntityPlayerSP player = mc.thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

            block.setBlockBoundsBasedOnState(mc.theWorld, blockPos);

            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                    .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                    .offset(-posX, -posY, -posZ);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() != 255 ? color.getAlpha() : 26);
        // drawFilledBox(axisAlignedBB);

        glLineWidth((float) width);
        enableGlCap(GL_LINE_SMOOTH);
        glColor(color);

        drawSelectionBoundingBox(axisAlignedBB);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDepthMask(true);
        resetCaps();
    }

}
