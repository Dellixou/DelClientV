package com.github.dellixou.delclientv3.utils.renderer;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;


public class RenderUtils {

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

    public static void highlightBlock(BlockPos pos, Color color, float opacity, float partialTicks) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double x = pos.getX() - (viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks);
        double y = pos.getY() - (viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks);
        double z = pos.getZ() - (viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, opacity);
        GlStateManager.enableDepth();
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

    public static void drawLineWithGL(Vec3 blockA, Vec3 blockB, boolean whiteLine, float red, float green, float blue) {

        double width = DelClient.settingsManager.getSettingById("user_route_width").getValDouble() * 0.1;

        if(whiteLine){
            GL11.glColor4f(255, 255, 255, 0F);
            GL11.glLineWidth(3);
        }else{
            GL11.glLineWidth((float) width);
            GL11.glColor4f(red, green, blue, 0F);
        }

        GL11.glBegin(GL11.GL_LINE_STRIP);

        GL11.glVertex3d(blockA.xCoord, blockA.yCoord, blockA.zCoord);
        GL11.glVertex3d(blockB.xCoord, blockB.yCoord, blockB.zCoord);

        GL11.glEnd();
    }

    public static Color rainbow(int delay) {
        float v1 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_1").getValDouble();
        float v2 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_2").getValDouble();

        double rainbowstate = Math.ceil((System.currentTimeMillis() + delay) / 20);
        rainbowstate %= 360;
        return Color.getHSBColor((float) (rainbowstate/360), v1/10, v2/10);
    }

}
