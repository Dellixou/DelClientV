package com.github.dellixou.delclientv3.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class DrawUtils {
    private static Minecraft mc = Minecraft.getMinecraft();
    private static final int[] DISPLAY_LISTS_2D = new int[4];
    private static final Map<Integer, Boolean> glCapMap = new HashMap<Integer, Boolean>();

    public static int getChroma(float speed, int offset) {
        return Color.HSBtoRGB(((System.currentTimeMillis() - offset * 10L) % (long) speed) / speed, 0.88F, 0.88F);
    }

    static {
        for (int i = 0; i < DISPLAY_LISTS_2D.length; i++) {
            DISPLAY_LISTS_2D[i] = glGenLists(1);
        }

        glNewList(DISPLAY_LISTS_2D[0], GL_COMPILE);

        quickDrawRect(-7F, 2F, -4F, 3F);
        quickDrawRect(4F, 2F, 7F, 3F);
        quickDrawRect(-7F, 0.5F, -6F, 3F);
        quickDrawRect(6F, 0.5F, 7F, 3F);

        glEndList();

        glNewList(DISPLAY_LISTS_2D[1], GL_COMPILE);

        quickDrawRect(-7F, 3F, -4F, 3.3F);
        quickDrawRect(4F, 3F, 7F, 3.3F);
        quickDrawRect(-7.3F, 0.5F, -7F, 3.3F);
        quickDrawRect(7F, 0.5F, 7.3F, 3.3F);

        glEndList();

        glNewList(DISPLAY_LISTS_2D[2], GL_COMPILE);

        quickDrawRect(4F, -20F, 7F, -19F);
        quickDrawRect(-7F, -20F, -4F, -19F);
        quickDrawRect(6F, -20F, 7F, -17.5F);
        quickDrawRect(-7F, -20F, -6F, -17.5F);

        glEndList();

        glNewList(DISPLAY_LISTS_2D[3], GL_COMPILE);

        quickDrawRect(7F, -20F, 7.3F, -17.5F);
        quickDrawRect(-7.3F, -20F, -7F, -17.5F);
        quickDrawRect(4F, -20.3F, 7.3F, -20F);
        quickDrawRect(-7.3F, -20.3F, -4F, -20F);

        glEndList();
    }

    public static void drawLineWithGL(BlockPos blockA, BlockPos blockB, boolean whiteLine, float red, float green,
                                      float blue) {

        double width = 3d;

        if (whiteLine) {
            GL11.glColor4f(255, 255, 255, 0F);
            GL11.glLineWidth(3);
        } else {
            GL11.glLineWidth((float) width);
            GL11.glColor4f(red, green, blue, 0F);
        }

        GL11.glBegin(GL11.GL_LINE_STRIP);

        GL11.glVertex3d(blockA.getX(), blockA.getY(), blockA.getZ());
        GL11.glVertex3d(blockB.getX(), blockB.getY(), blockB.getZ());

        GL11.glEnd();
    }

    public static void drawLineWithGL(Vec3 blockA, Vec3 blockB, boolean whiteLine, float red, float green,
                                      float blue) {

        double width = 3d;

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

    public static void renderTextInWorld(Minecraft mc, String text, double x, double y, double z, float partialTicks,
                                         float scale) {
        Vec3 playerPos = mc.thePlayer.getPositionEyes(partialTicks);
        double distance = playerPos.distanceTo(new Vec3(x, y, z));
        float maxDistance = 200.0f;
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

        GlStateManager.pushMatrix();
        GlStateManager.translate(x - playerPos.xCoord, y - playerPos.yCoord, z - playerPos.zCoord);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        if (true) {
            GL11.glDisable(GL11.GL_DEPTH_TEST); // Draw the text on top of the geometry
        }
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        // Calculate the color with alpha transparency
        int alpha = (int) (opacity * 255.0f);
        int color = (alpha << 24) | 0xFFFFFF; // Add the opacity to the color code

        // Render the text
        int glyphWidth = mc.fontRendererObj.getStringWidth(text);
        int glyphHeight = mc.fontRendererObj.FONT_HEIGHT;

        // Define margin
        int margin = 4;

        // Draw the background rectangle with margin + text

        mc.fontRendererObj.drawString(text, -glyphWidth / 2, -glyphHeight / 2, color, false);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawPixelBox(final Vec3 vec, final Color color, final double size, float partialTicks) {
        final RenderManager renderManager = mc.getRenderManager();

        final double x = vec.xCoord - renderManager.viewerPosX;
        final double y = vec.yCoord - renderManager.viewerPosY;
        final double z = vec.zCoord - renderManager.viewerPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + size, y + size, z + size);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), 35);
        // drawFilledBox(axisAlignedBB);

        glLineWidth(3F);
        enableGlCap(GL_LINE_SMOOTH);
        glColor(color);

        drawSelectionBoundingBox(axisAlignedBB);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDepthMask(true);
        resetCaps();
    }

    public static void disableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, false);
    }

    public static void glColor(final Color color) {
        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GL11.glColor4f(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void resetCaps() {
        glCapMap.forEach(DrawUtils::setGlState);
    }

    public static void enableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void disableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void setGlCap(final int cap, final boolean state) {
        glCapMap.put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }

    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }

    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        // Lower Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();

        tessellator.draw();
    }

    public static void quickDrawRect(final float x, final float y, final float x2, final float y2) {
        glBegin(GL_QUADS);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();
    }

    public static void highlightBlock(BlockPos pos, Color color, float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double x = pos.getX() - (viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks);
        double y = pos.getY() - (viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks);
        double z = pos.getZ() - (viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks);

        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, 1f);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    public static void highlightBlock(BlockPos pos, Color color, float opacity, float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double x = pos.getX() - (viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks);
        double y = pos.getY() - (viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks);
        double z = pos.getZ() - (viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, opacity);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
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

    public void drawOutlinedRect(int x, int y, int width, int height, Color fill, Color outline) {
        GlStateManager.pushMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.disableTexture2D();

        int xEnd = x + width;
        int yEnd = y + height;

        Gui.drawRect(x, y, xEnd, yEnd, fill.getRGB());

        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.disableTexture2D();

        GL11.glLineWidth(2f);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        float[] outlineColor = translateToFloat(outline);

        GlStateManager.color(outlineColor[0], outlineColor[1], outlineColor[2], 1f);

        GL11.glBegin(GL11.GL_LINE_LOOP);

        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, yEnd);
        GL11.glVertex2d(xEnd, yEnd);
        GL11.glVertex2d(xEnd, y);

        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GlStateManager.enableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
        GlStateManager.popMatrix();
    }

    public static void renderTracer(double posX, double posY, double posZ, double height, Color color,
                                    float partialTicks) {
        Entity render = mc.getRenderViewEntity();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        final double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks;
        final double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks;
        final double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-realX, -realY, -realZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glDisable(3553);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GL11.glLineWidth(2f);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);

        worldRenderer.pos(realX, realY + render.getEyeHeight(), realZ)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldRenderer.pos(posX, posY, posZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .endVertex();

        Tessellator.getInstance().draw();

        GlStateManager.translate(realX, realY, realZ);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    public static void renderEspBox(BlockPos blockPos, float partialTicks, int color) {
        renderEspBox(blockPos, partialTicks, color, 0.5f);
    }

    public static void renderEspBox(BlockPos blockPos, float partialTicks, int color, float opacity) {
        if (blockPos != null) {
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);

            if (blockState != null) {
                Block block = blockState.getBlock();
                block.setBlockBoundsBasedOnState(mc.theWorld, blockPos);
                double d0 = mc.thePlayer.lastTickPosX
                        + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * (double) partialTicks;
                double d1 = mc.thePlayer.lastTickPosY
                        + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double) partialTicks;
                double d2 = mc.thePlayer.lastTickPosZ
                        + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * (double) partialTicks;
                drawFilledBoundingBox(block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(0.002D, 0.002D, 0.002D)
                        .offset(-d0, -d1, -d2), color, opacity);
            }
        }
    }

    public static void drawFilledBoundingBox(AxisAlignedBB aabb, int color, float opacity) {
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        GlStateManager.color(r, g, b, a * opacity);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        tessellator.draw();
        GlStateManager.color(r, g, b, a * opacity);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        GlStateManager.color(r, g, b, a * opacity);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        tessellator.draw();
        GlStateManager.color(r, g, b, a);
        RenderGlobal.drawSelectionBoundingBox(aabb);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void drawTargetESP(Entity target, Color color, float partialTicks) {
        double z;
        double x;
        int i;
        GL11.glPushMatrix();
        float location = (float) ((Math.sin((double) System.currentTimeMillis() * 0.005) + 1.0) * 0.5);
        GlStateManager.translate(
                target.lastTickPosX + (target.posX - target.lastTickPosX) * (double) partialTicks
                        - mc.getRenderManager().viewerPosX,
                target.lastTickPosY + (target.posY - target.lastTickPosY) * (double) partialTicks
                        - mc.getRenderManager().viewerPosY + (double) (target.height * location),
                target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * (double) partialTicks
                        - mc.getRenderManager().viewerPosZ);
        enableGL2D();
        GL11.glShadeModel(7425);
        GL11.glDisable(2884);
        GL11.glLineWidth(3.0f);
        GL11.glBegin(3);
        double cos = Math.cos((double) System.currentTimeMillis() * 0.005);
        for (i = 0; i <= 120; ++i) {
            GL11.glColor4f((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f,
                    (float) color.getBlue() / 255.0f, 1.0f);
            x = Math.cos((double) i * Math.PI / 60.0) * (double) target.width;
            z = Math.sin((double) i * Math.PI / 60.0) * (double) target.width;
            GL11.glVertex3d(x, (double) 0.15f * cos, z);
        }
        GL11.glEnd();
        GL11.glBegin(5);
        for (i = 0; i <= 120; ++i) {
            GL11.glColor4f((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f,
                    (float) color.getBlue() / 255.0f, 0.5f);
            x = Math.cos((double) i * Math.PI / 60.0) * (double) target.width;
            z = Math.sin((double) i * Math.PI / 60.0) * (double) target.width;
            GL11.glVertex3d(x, (double) 0.15f * cos, z);
            GL11.glColor4f((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f,
                    (float) color.getBlue() / 255.0f, 0.2f);
            GL11.glVertex3d(x, (double) -0.15f * cos, z);
        }
        GL11.glEnd();
        GL11.glShadeModel(7424);
        GL11.glEnable(2884);
        disableGL2D();
        GL11.glPopMatrix();
    }

    public static float[] translateToFloat(Color color) {
        return new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
                color.getAlpha() / 255f};
    }

    public static void entityESPBox(Entity entity, float partialTicks, Color color) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(1.5f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        setColor(color);
        RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(
                entity.getEntityBoundingBox().minX - entity.posX
                        + (entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks
                        - Minecraft.getMinecraft().getRenderManager().viewerPosX),
                entity.getEntityBoundingBox().minY - entity.posY
                        + (entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks
                        - Minecraft.getMinecraft().getRenderManager().viewerPosY),
                entity.getEntityBoundingBox().minZ - entity.posZ
                        + (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks
                        - Minecraft.getMinecraft().getRenderManager().viewerPosZ),
                entity.getEntityBoundingBox().maxX - entity.posX
                        + (entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks
                        - Minecraft.getMinecraft().getRenderManager().viewerPosX),
                entity.getEntityBoundingBox().maxY - entity.posY
                        + (entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks
                        - Minecraft.getMinecraft().getRenderManager().viewerPosY),
                entity.getEntityBoundingBox().maxZ - entity.posZ
                        + (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks
                        - Minecraft.getMinecraft().getRenderManager().viewerPosZ)));
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static void setColor(Color c) {
        GL11.glColor4f((float) c.getRed() / 255.0f, (float) c.getGreen() / 255.0f, (float) c.getBlue() / 255.0f,
                (float) c.getAlpha() / 255.0f);
    }

    public static void enableChams() {
        GL11.glEnable(32823);
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1000000.0f);
    }

    public static void disableChams() {
        GL11.glDisable(32823);
        GlStateManager.doPolygonOffset(1.0f, 1000000.0f);
        GlStateManager.disablePolygonOffset();
    }

    public static void miniBlockBox(BlockPos block, Color color) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        setColor(color);
        Minecraft.getMinecraft().getRenderManager();
        RenderGlobal.drawSelectionBoundingBox(
                new AxisAlignedBB(block.getX() - 0.05 - Minecraft.getMinecraft().getRenderManager().viewerPosX,
                        block.getY() - 0.05 - Minecraft.getMinecraft().getRenderManager().viewerPosY,
                        block.getZ() - 0.05 - Minecraft.getMinecraft().getRenderManager().viewerPosZ,
                        block.getX() + 0.05 - Minecraft.getMinecraft().getRenderManager().viewerPosX,
                        block.getY() + 0.05 - Minecraft.getMinecraft().getRenderManager().viewerPosY,
                        block.getZ() + 0.05 - Minecraft.getMinecraft().getRenderManager().viewerPosZ));
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static void drawNametag(String str) {
        FontRenderer fontrenderer = mc.fontRendererObj;
        float f1 = 0.0266666688f;
        GlStateManager.pushMatrix();
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferBuilder = tessellator.getWorldRenderer();
        int i = 0;

        int j = fontrenderer.getStringWidth(str) / 2;
        GlStateManager.disableTexture2D();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(-j - 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferBuilder.pos(-j - 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferBuilder.pos(j + 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferBuilder.pos(j + 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127);
        GlStateManager.depthMask(true);

        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);

        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void renderTracer(Entity entity, Color color, float partialTicks) {
        renderTracer(entity.posX, entity.posY, entity.posZ, entity.height, color, partialTicks);
    }
}
