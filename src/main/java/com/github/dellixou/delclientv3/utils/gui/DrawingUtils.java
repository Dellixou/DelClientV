package com.github.dellixou.delclientv3.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class DrawingUtils {

    // Renderer Infos
    public static final double TWICE_PI = Math.PI*2;
    private static Tessellator tessellator = Tessellator.getInstance();
    private static WorldRenderer worldRenderer = tessellator.getWorldRenderer();


    /** Draws a scaled image to the screen */
    public static void drawScaledImage(float xCoord, float yCoord, int xLoc, int yLoc, int xSize, int ySize, float scale, ResourceLocation image, double red, double green, double blue, float imageWidth, float imageHeight) {
        drawScaledImage(xCoord, yCoord, xLoc, yLoc, xSize, ySize, scale, image, red, green, blue, 1, imageWidth, imageHeight);
    }

    /** Draws a scaled image to the screen */
    public static void drawScaledImage(float xCoord, float yCoord, int xLoc, int yLoc, int xSize, int ySize
            , float scale, ResourceLocation image, double red, double green, double blue, double alpha, float imageWidth
            , float imageHeight) {
        Gui gui = Minecraft.getMinecraft().currentScreen;

        if (gui == null)
            return;

        GL11.glPushMatrix();

        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        GL11.glColor4d(red/0xff, green/0xff, blue/0xff, alpha);
        GL11.glEnable(3042);
        GL11.glTranslatef(xCoord, yCoord, 1.0F);
        GL11.glScaled(scale, scale, 1.0F);

        Gui.drawModalRectWithCustomSizedTexture(0, 0, xLoc, yLoc, xSize, ySize, imageWidth, imageHeight);
        GL11.glColor4d(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    /**
     * Draws a rounded rectangle on the screen
     *
     * @param xCoord The x coordinate where the rectangle will be on the screen
     * @param yCoord The y coordinate where the rectangle will be on the screen
     * @param xSize The width of the rectangle
     * @param ySize The height of the rectangle
     * @param colour The colour of the rectangle
     * @param text The string of text that will be drawn on the rectangle
     */
    public static void drawRoundedRect(int xCoord, int yCoord, int xSize, int ySize, int colour, String text) {
        int width = xCoord + xSize;
        int height = yCoord + ySize;

        // Top rounding
        Gui.drawRect(xCoord + 1, yCoord, width - 1, height, colour);

        // Middle rect
        Gui.drawRect(xCoord, yCoord + 1, width, height - 1, colour);

        DrawingUtils.drawCenteredUnicodeString(text, xCoord + (xSize / 2), yCoord, 0xFFFFFF);
    }

    /**
    public static void drawRoundedRectTop(int xCoord, int yCoord, int xSize, int ySize, int colour, String text) {
        int width = xCoord + xSize;
        int height = yCoord + ySize;

        // Top rounding
        Gui.drawRect(xCoord + 1, yCoord, width - 1, height, colour);

        // Middle rect
        Gui.drawRect(xCoord, yCoord + 1, width, height, colour);

        DrawingUtils.drawCenteredUnicodeString(text, xCoord + (xSize / 2), yCoord, 0xFFFFFF);
    }
     */

    public static void drawRect(double left, double top, double right, double bottom, int color)
    {
        if (left < right)
        {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)left, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)top, 0.0D).endVertex();
        worldrenderer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawArc(double x, double y, double radius, double startAngle, double endAngle, int color) {
        // Dessiner un arc avec des lignes
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color((float)(color >> 16 & 255) / 255.0F, (float)(color >> 8 & 255) / 255.0F, (float)(color & 255) / 255.0F, (float)(color >> 24 & 255) / 255.0F);
        worldrenderer.begin(6, DefaultVertexFormats.POSITION);
        for (double angle = startAngle; angle < endAngle; angle += 5) { // Utilisez un angle plus petit pour un rendu plus lisse
            double rad = Math.toRadians(angle);
            double x2 = x + radius * Math.cos(rad);
            double y2 = y + radius * Math.sin(rad);
            worldrenderer.pos(x2, y2, 0.0D).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawSmoothArc(int x, int y, int radius, int startAngle, int endAngle, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color((float)(color >> 16 & 255) / 255.0F, (float)(color >> 8 & 255) / 255.0F, (float)(color & 255) / 255.0F, (float)(color >> 24 & 255) / 255.0F);

        double angleStep = Math.PI / 180.0; // Angle step for drawing curve
        double centerX = x + radius;
        double centerY = y + radius;

        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        for (int angle = startAngle; angle <= endAngle; angle++) {
            double angleRad = Math.toRadians(angle);
            double nextAngleRad = Math.toRadians(angle + 1);
            double startX = centerX + radius * Math.cos(angleRad);
            double startY = centerY + radius * Math.sin(angleRad);
            double endX = centerX + radius * Math.cos(nextAngleRad);
            double endY = centerY + radius * Math.sin(nextAngleRad);

            double controlX1 = centerX + radius * Math.cos(angleRad + angleStep);
            double controlY1 = centerY + radius * Math.sin(angleRad + angleStep);
            double controlX2 = centerX + radius * Math.cos(nextAngleRad - angleStep);
            double controlY2 = centerY + radius * Math.sin(nextAngleRad - angleStep);

            worldrenderer.pos(startX, startY, 0.0D).endVertex();
            worldrenderer.pos(controlX1, controlY1, 0.0D).endVertex();
            worldrenderer.pos(controlX2, controlY2, 0.0D).endVertex();
            worldrenderer.pos(endX, endY, 0.0D).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawCircle(double x, double y, double radius, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color((float)(color >> 16 & 255) / 255.0F, (float)(color >> 8 & 255) / 255.0F, (float)(color & 255) / 255.0F, (float)(color >> 24 & 255) / 255.0F);
        worldrenderer.begin(6, DefaultVertexFormats.POSITION);
        for (int i = 0; i <= 360; i += 10) {
            double rad = Math.toRadians(i);
            double x2 = x + radius * Math.cos(rad);
            double y2 = y + radius * Math.sin(rad);
            worldrenderer.pos(x2, y2, 0.0D).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Draws a centered unicode string on the screen
     *
     * @param text The text that will be drawn to the screen
     * @param xCoord The x position of the text
     * @param yCoord The y position of the text
     * @param colour The colour of the text
     */
    public static void drawCenteredUnicodeString(String text, int xCoord, int yCoord, int colour) {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        boolean prevFlag;

        // Remembering unicode flag
        prevFlag = font.getUnicodeFlag();

        font.setUnicodeFlag(true);
        font.drawString(text, xCoord - (font.getStringWidth(text) / 2), yCoord, colour);
        font.setUnicodeFlag(prevFlag);
    }

    /**
     * Draws a scaled string
     *
     * @param text The text that will be drawn
     * @param xCoord The x position of where the text will be drawn
     * @param yCoord The y position of where the text will be drawn
     * @param scale The scale of the text
     * @param colour The colour of the text
     * @param unicodeFlag If the text should be in unicode
     */
    public static void drawScaledString(String text, int xCoord, int yCoord, float scale, int colour, boolean unicodeFlag) {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        boolean prevFlag;

        // Remembering unicode flag
        prevFlag = font.getUnicodeFlag();

        font.setUnicodeFlag(unicodeFlag);

        GL11.glPushMatrix();

        // Positioning text
        GL11.glTranslated(xCoord, yCoord, 0.0);

        // Scaling text
        GL11.glScalef(scale, scale, 1.0F);

        // Drawing text
        font.drawString(text, 0, 0, colour);
        GL11.glPopMatrix();

        font.setUnicodeFlag(prevFlag);
    }

    /**
     * Draws a small unicode string on the screen that wraps
     *
     * @param text The text that will be drawn on the screen
     * @param xCoord The x position of the text
     * @param yCoord The y position of the text
     * @param colour The colour of the text
     * @param width The width of the string before it wraps
     */
    public static void drawSplitUnicodeString(String text, int xCoord, int yCoord, int colour, int width) {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        boolean prevFlag;

        // Remembering unicode flag
        prevFlag = font.getUnicodeFlag();

        font.setUnicodeFlag(true);
        font.drawSplitString(text, xCoord, yCoord, width, colour);
        font.setUnicodeFlag(prevFlag);
    }

    /**
     * Draws a small unicode string on the screen
     *
     * @param text The text that will be drawn to the screen
     * @param xCoord The x position of the text
     * @param yCoord The y position of the text
     * @param colour The colour of the text
     */
    public static void drawUnicodeString(String text, int xCoord, int yCoord, int colour) {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        boolean prevFlag;

        // Remembering unicode flag
        prevFlag = font.getUnicodeFlag();

        font.setUnicodeFlag(true);
        font.drawString(text, xCoord, yCoord, colour);
        font.setUnicodeFlag(prevFlag);
    }

    /**
     * Gets the length of a string of unicode characters
     *
     * @param unicodeString a String of unicode characters
     * @return the length of the unicodeString
     */
    public static int getUnicodeStringWidth(String unicodeString) {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        boolean prevFlag;
        int stringLength;

        // Storing previous unicode flag
        prevFlag = font.getUnicodeFlag();

        font.setUnicodeFlag(true);
        stringLength = font.getStringWidth(unicodeString);
        font.setUnicodeFlag(prevFlag);

        return stringLength;
    }

    /**
     * Makes an image circular
     *
     * @param image The image to make circular
     * @return a circular image of the one passed in
     */
    public static BufferedImage circularize(BufferedImage image)
    {
        // Making image circular
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = out.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.BLACK); // The color here doesn't really matter
            graphics.fillOval(0, 0, image.getWidth(), image.getHeight());

            graphics.setComposite(AlphaComposite.SrcIn); // Only paint inside the oval from now on
            graphics.drawImage(image, 0, 0, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally {
            graphics.dispose();
        }

        return out;
    }

    public static void drawCircle(double x, double y, int radius, int sides)
    {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldRenderer.pos(x, y, 0).endVertex();

        for(int i = 0; i <= sides ;i++)
        {
            double angle = (TWICE_PI * i / sides) + Math.toRadians(180);
            worldRenderer.pos(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).endVertex();
        }
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);


    }

    /////////////////////////////////////////////////////////////////////////////////

    private static final List<Integer> csBuffer;

    static {
        csBuffer = new ArrayList<Integer>();
        GL11.glEnableClientState(99999);
        GL11.glEnableClientState(99999);
    }

    public static void startSmooth() {
        GL11.glEnable((int)2848);
        GL11.glEnable((int)2881);
        GL11.glEnable((int)2832);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glHint((int)3154, (int)4354);
        GL11.glHint((int)3155, (int)4354);
        GL11.glHint((int)3153, (int)4354);
    }

    public static void endSmooth() {
        GL11.glDisable((int)2848);
        GL11.glDisable((int)2881);
        GL11.glEnable((int)2832);
    }

    public static int width() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
    }

    public static int height() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
    }


    public static int getHexRGB(final int hex) {
        return 0xFF000000 | hex;
    }

    public static void doGlScissor(float x, float y, float width, float height) {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;
        if (k == 0) {
            k = 1000;
        }
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320
                && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glScissor((int) (x * scaleFactor), (int) (mc.displayHeight - (y + height) * scaleFactor),
                (int) (width * scaleFactor), (int) (height * scaleFactor));
    }

    public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (float) (col1 >> 24 & 255) / 255.0f;
        float f1 = (float) (col1 >> 16 & 255) / 255.0f;
        float f2 = (float) (col1 >> 8 & 255) / 255.0f;
        float f3 = (float) (col1 & 255) / 255.0f;
        float f4 = (float) (col2 >> 24 & 255) / 255.0f;
        float f5 = (float) (col2 >> 16 & 255) / 255.0f;
        float f6 = (float) (col2 >> 8 & 255) / 255.0f;
        float f7 = (float) (col2 & 255) / 255.0f;
        GL11.glEnable((int) 3042);
        GL11.glDisable((int) 3553);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glEnable((int) 2848);
        GL11.glShadeModel((int) 7425);
        GL11.glPushMatrix();
        GL11.glBegin((int) 7);
        GL11.glColor4f((float) f1, (float) f2, (float) f3, (float) f);
        GL11.glVertex2d((double) left, (double) top);
        GL11.glVertex2d((double) left, (double) bottom);
        GL11.glColor4f((float) f5, (float) f6, (float) f7, (float) f4);
        GL11.glVertex2d((double) right, (double) bottom);
        GL11.glVertex2d((double) right, (double) top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        GL11.glDisable((int) 2848);
        GL11.glShadeModel((int) 7424);
    }

    public static void drawRainbow(double x, double y, double x1, double y1, int col1, int col2, int col3, int col4) {
        Color c1 = new Color(col1);
        Color c2 = new Color(col2);
        Color c3 = new Color(col3);
        Color c4 = new Color(col4);
        GL11.glEnable((int) 3042);
        GL11.glDisable((int) 3553);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glEnable((int) 2848);
        GL11.glShadeModel((int) 7425);
        GL11.glPushMatrix();
        GL11.glBegin((int) 7);
        //左上角开始绘制 逆时针
        GL11.glColor4f((float) c1.getRed(), (float) c1.getGreen(), (float) c1.getBlue(), (float) c1.getAlpha());
        GL11.glVertex2d((double) x, (double) y);
        GL11.glColor4f((float) c2.getRed(), (float) c2.getGreen(), (float) c2.getBlue(), (float) c2.getAlpha());
        GL11.glVertex2d((double) x, (double) y1);
        GL11.glColor4f((float) c3.getRed(), (float) c3.getGreen(), (float) c3.getBlue(), (float) c3.getAlpha());
        GL11.glVertex2d((double) x1, (double) y1);
        GL11.glColor4f((float) c4.getRed(), (float) c4.getGreen(), (float) c4.getBlue(), (float) c4.getAlpha());
        GL11.glVertex2d((double) x1, (double) y);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        GL11.glDisable((int) 2848);
        GL11.glShadeModel((int) 7424);
    }

    public static void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void enableGL2D() {
        GL11.glDisable((int) 2929);
        GL11.glEnable((int) 3042);
        GL11.glDisable((int) 3553);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glDepthMask((boolean) true);
        GL11.glEnable((int) 2848);
        GL11.glHint((int) 3154, (int) 4354);
        GL11.glHint((int) 3155, (int) 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        GL11.glEnable((int) 2929);
        GL11.glDisable((int) 2848);
        GL11.glHint((int) 3154, (int) 4352);
        GL11.glHint((int) 3155, (int) 4352);
    }

    public static void drawGradientRect(float x, float y, float x1, float y1, int topColor, int bottomColor) {
        enableGL2D();
        GL11.glShadeModel((int) 7425);
        GL11.glBegin((int) 7);
        glColor(topColor);
        GL11.glVertex2f((float) x, (float) y1);
        GL11.glVertex2f((float) x1, (float) y1);
        glColor(bottomColor);
        GL11.glVertex2f((float) x1, (float) y);
        GL11.glVertex2f((float) x, (float) y);
        GL11.glEnd();
        GL11.glShadeModel((int) 7424);
        disableGL2D();
    }

    public static void drawCustomImage(int x, int y, int width, int height, ResourceLocation image) {
        Minecraft mc = Minecraft.getMinecraft();
        IResourceManager resourceManager = mc.getResourceManager();

        try {
            resourceManager.getResource(image);
            mc.getTextureManager().bindTexture(image);

            // Set up OpenGL to draw the image with transparency
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // Enable linear and trilinear filtering
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            // Set color with 80% transparency
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.87F);

            // Draw the image
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

            // Restore OpenGL state
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error: resource not found
        }
    }

    public static void drawCustomImageAlpha(float v, float v1, int i, int i1, ResourceLocation resourceLocation, int i2, float alpha) {
        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(new Color(i2).getRed() / 255f, new Color(i2).getGreen() / 255f, new Color(i2).getBlue() / 255f, alpha / 255f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        Gui.drawModalRectWithCustomSizedTexture((int) v, (int) v1, 0.0f, 0.0f, i, i1, (float) i, (float) i1);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void drawCustomImage(float x, float y, int width, int height, ResourceLocation image) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable((int) 2929);
        GL11.glEnable((int) 3042);
        GL11.glDepthMask((boolean) false);
        OpenGlHelper.glBlendFunc((int) 770, (int) 771, (int) 1, (int) 0);
        GL11.glColor4f((float) 1.0f, (float) 1.0f, (float) 1.0f, (float) 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, (float) 0.0f, (float) 0.0f, (int) width, (int) height, (float) width, (float) height);
        GL11.glDepthMask((boolean) true);
        GL11.glDisable((int) 3042);
        GL11.glEnable((int) 2929);
    }

    public static void drawCustomImage(float x, float y, float width, float height, ResourceLocation image) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.85f); // Set color with 80% transparency
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, 0.0f, 0.0f, (int) width, (int) height, width, height);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }


    public static void drawBorderedRect(final float x, final float y, final float x2, final float y2, final float l1, final int col1, final int col2) {
        Gui.drawRect((int) x, (int) y, (int) x2, (int) y2, col2);
        final float f = (col1 >> 24 & 0xFF) / 255.0f;
        final float f2 = (col1 >> 16 & 0xFF) / 255.0f;
        final float f3 = (col1 >> 8 & 0xFF) / 255.0f;
        final float f4 = (col1 & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(f2, f3, f4, f);
        GL11.glLineWidth(l1);
        GL11.glBegin(1);
        GL11.glVertex2d((double) x, (double) y);
        GL11.glVertex2d((double) x, (double) y2);
        GL11.glVertex2d((double) x2, (double) y2);
        GL11.glVertex2d((double) x2, (double) y);
        GL11.glVertex2d((double) x, (double) y);
        GL11.glVertex2d((double) x2, (double) y);
        GL11.glVertex2d((double) x, (double) y2);
        GL11.glVertex2d((double) x2, (double) y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static void pre() {
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
    }

    public static void post() {
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glColor3d(1.0, 1.0, 1.0);
    }

    public static void stopDrawing() {
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static Color blend(final Color color1, final Color color2, final double ratio) {
        final float r = (float) ratio;
        final float ir = 1.0f - r;
        final float[] rgb1 = new float[3];
        final float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        final Color color3 = new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);
        return color3;
    }

    public static void setupRender(final boolean start) {
        if (start) {
            GlStateManager.enableBlend();
            GL11.glEnable(2848);
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(770, 771);
            GL11.glHint(3154, 4354);
        } else {
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GL11.glDisable(2848);
            GlStateManager.enableDepth();
        }
        GlStateManager.depthMask(!start);
    }

    public static void circle(final float x, final float y, final float radius, final int fill) {
        arc(x, y, 0.0f, 360.0f, radius, fill);
    }

    public static void circleOutline(final float x, final float y, final float radius, final int fill) {
        arcOutline(x, y, 0.0f, 360.0f, radius, fill);
    }

    public static void circle(float x, float y, float radius, Color fill) {
        arc(x, y, 0.0f, 360.0f, radius, fill);
    }

    public static void drawCircle(float x, float y, float radius, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
//        GL11.glEnable(GL_POLYGON_SMOOTH);

        glColor4f(red, green, blue, alpha);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glPushMatrix();
        glLineWidth(1F);
        glBegin(GL_POLYGON);
        for(int i = 0; i <= 360; i++)
            glVertex2d(x + Math.sin(i * Math.PI / 180.0D) * radius, y + Math.cos(i * Math.PI / 180.0D) * radius);
        glEnd();
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_LINE_SMOOTH);
        glColor4f(1F, 1F, 1F, 1F);
//        GL11.glDisable(GL_POLYGON_SMOOTH);

    }

    public static void smoothCircle(final float x, final float y, final float radius, final Color c) {
//        GL11.glEnable(GL_MULTISAMPLE);
        GL11.glEnable(GL_POLYGON_SMOOTH);
        for (int i2 = 0; i2 < 3; i2++) {
            float alpha = (float) (c.getRGB() >> 24 & 255) / 255.0f;
            float red = (float) (c.getRGB() >> 16 & 255) / 255.0f;
            float green = (float) (c.getRGB() >> 8 & 255) / 255.0f;
            float blue = (float) (c.getRGB() & 255) / 255.0f;
            boolean blend = GL11.glIsEnabled((int) 3042);
            boolean line = GL11.glIsEnabled((int) 2848);
            boolean texture = GL11.glIsEnabled((int) 3553);
            if (!blend) {
                GL11.glEnable((int) 3042);
            }
            if (!line) {
                GL11.glEnable((int) 2848);
            }
            if (texture) {
                GL11.glDisable((int) 3553);
            }
            GL11.glBlendFunc((int) 770, (int) 771);
            GL11.glColor4f((float) red, (float) green, (float) blue, (float) alpha);
            GL11.glBegin((int) 9);
            int i = 0;
            while (i <= 360) {
                GL11.glVertex2d(
                        (double) ((double) x + Math.sin((double) ((double) i * 3.141526 / 180.0)) * (double) radius),
                        (double) ((double) y + Math.cos((double) ((double) i * 3.141526 / 180.0)) * (double) radius));
                ++i;
            }
            GL11.glEnd();
            if (texture) {
                GL11.glEnable((int) 3553);
            }
            if (!line) {
                GL11.glDisable((int) 2848);
            }
            if (!blend) {
                GL11.glDisable((int) 3042);
            }
        }
        GL11.glDisable(GL_POLYGON_SMOOTH);
        GL11.glClear(0);
    }

    public static void smoothCircle(final float x, final float y, final float radius, final int c) {
        //GL11.glEnable(GL_POLYGON_SMOOTH);
        for (int i2 = 0; i2 < 3; i2++) {
            float alpha = (float) (c >> 24 & 255) / 255.0f;
            float red = (float) (c >> 16 & 255) / 255.0f;
            float green = (float) (c >> 8 & 255) / 255.0f;
            float blue = (float) (c & 255) / 255.0f;
            boolean blend = GL11.glIsEnabled((int) 3042);
            boolean line = GL11.glIsEnabled((int) 2848);
            boolean texture = GL11.glIsEnabled((int) 3553);
            if (!blend) {
                GL11.glEnable((int) 3042);
            }
            if (!line) {
                GL11.glEnable((int) 2848);
            }
            if (texture) {
                GL11.glDisable((int) 3553);
            }
            GL11.glBlendFunc((int) 770, (int) 771);
            GL11.glColor4f((float) red, (float) green, (float) blue, (float) alpha);
            GL11.glBegin((int) 9);
            int i = 0;
            while (i <= 360) {
                GL11.glVertex2d(
                        (double) ((double) x + Math.sin((double) ((double) i * 3.141526 / 180.0)) * (double) radius),
                        (double) ((double) y + Math.cos((double) ((double) i * 3.141526 / 180.0)) * (double) radius));
                ++i;
            }
            GL11.glEnd();
            if (texture) {
                GL11.glEnable((int) 3553);
            }
            if (!line) {
                GL11.glDisable((int) 2848);
            }
            if (!blend) {
                GL11.glDisable((int) 3042);
            }
        }
        //GL11.glDisable(GL_POLYGON_SMOOTH);
        GL11.glClear(0);
    }

    public static void arc(final float x, final float y, final float start, final float end, final float radius,
                           final int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arcOutline(final float x, final float y, final float start, final float end, final float radius,
                           final int color) {
        arcEllipseOutline(x, y, start, end, radius, radius, color);
    }

    public static void arc(final float x, final float y, final float start, final float end, final float radius,
                           final Color color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h,
                                  final int color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp = 0.0f;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final float var11 = (color >> 24 & 0xFF) / 255.0f;
        final float var12 = (color >> 16 & 0xFF) / 255.0f;
        final float var13 = (color >> 8 & 0xFF) / 255.0f;
        final float var14 = (color & 0xFF) / 255.0f;
        final Tessellator var15 = Tessellator.getInstance();
        var15.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var12, var13, var14, var11);
        if (var11 > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for (float i = end; i >= start; i -= 0.5f) { // float i = end; i >= start; i -= 4.0f ------- 1.0f
                final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w * 1.00f;
                final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h * 1.00f; // 1.001f
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        for (float i = end; i >= start; i -= 1.0f) { // float i = end; i >= start; i -= 4.0f
            final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w;
            final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    public static void arcEllipseOutline(final float x, final float y, float start, float end, final float w, final float h,
                                  final int color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp = 0.0f;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final float var11 = (color >> 24 & 0xFF) / 255.0f;
        final float var12 = (color >> 16 & 0xFF) / 255.0f;
        final float var13 = (color >> 8 & 0xFF) / 255.0f;
        final float var14 = (color & 0xFF) / 255.0f;
        final Tessellator var15 = Tessellator.getInstance();
        var15.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var12, var13, var14, var11);
        if (var11 > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for (float i = start; i <= end; i += 1.0f) { // float i = end; i >= start; i -= 4.0f
                final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w * 1.001f;
                final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        for (float i = start; i <= end; i += 1.0f) { // float i = end; i >= start; i -= 4.0f
            final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w;
            final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h,
                                  final Color color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp = 0.0f;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final Tessellator var9 = Tessellator.getInstance();
        var9.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f);
        if (color.getAlpha() > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for (float i = end; i >= start; i -= 4.0f) {
                final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w * 1.001f;
                final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        for (float i = end; i >= start; i -= 4.0f) {
            final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w;
            final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRect(float x, float y, float x1, float y1, int color) {
        Gui.drawRect((int) x, (int) y, (int) x1, (int) y1, color);
    }

    public static void drawRect(float x, float y, double x1, float y1, Color color) {
        Gui.drawRect((int) x, (int) y, (int) x1, (int) y1, color.getRGB());
    }

    public static void drawRoundedRect(float x, float y, float x2, float y2, final float round, final int color) {
        x += (float) (round / 2.0f + 0.5);
        y += (float) (round / 2.0f + 0.5);
        x2 -= (float) (round / 2.0f + 0.5);
        y2 -= (float) (round / 2.0f + 0.5);
        Gui.drawRect((int) x, (int) y, (int) x2, (int) y2, color);
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
        circle(x + round / 2.0f, y + round / 2.0f, round, color);
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
        Gui.drawRect((int) (x - round / 2.0f - 0.5f), (int) (y + round / 2.0f), (int) x2, (int) (y2 - round / 2.0f),
                color);
        Gui.drawRect((int) x, (int) (y + round / 2.0f), (int) (x2 + round / 2.0f + 0.5f), (int) (y2 - round / 2.0f),
                color);
        Gui.drawRect((int) (x + round / 2.0f), (int) (y - round / 2.0f - 0.5f), (int) (x2 - round / 2.0f),
                (int) (y2 - round / 2.0f), color);
        Gui.drawRect((int) (x + round / 2.0f), (int) y, (int) (x2 - round / 2.0f), (int) (y2 + round / 2.0f + 0.5f),
                color);
    }

    public static void drawRoundedRect(float x, float y, float x2, float y2, final float round, final Color color) {
        x += (float) (round / 2.0f + 0.5);
        y += (float) (round / 2.0f + 0.5);
        x2 -= (float) (round / 2.0f + 0.5);
        y2 -= (float) (round / 2.0f + 0.5);
        Gui.drawRect((int) x, (int) y, (int) x2, (int) y2, color.getRGB());
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
        circle(x + round / 2.0f, y + round / 2.0f, round, color);
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
        Gui.drawRect((int) (x - round / 2.0f - 0.5f), (int) (y + round / 2.0f), (int) x2, (int) (y2 - round / 2.0f),
                color.getRGB());
        Gui.drawRect((int) x, (int) (y + round / 2.0f), (int) (x2 + round / 2.0f + 0.5f), (int) (y2 - round / 2.0f),
                color.getRGB());
        Gui.drawRect((int) (x + round / 2.0f), (int) (y - round / 2.0f - 0.5f), (int) (x2 - round / 2.0f),
                (int) (y2 - round / 2.0f), color.getRGB());
        Gui.drawRect((int) (x + round / 2.0f), (int) y, (int) (x2 - round / 2.0f), (int) (y2 + round / 2.0f + 0.5f),
                color.getRGB());
    }


    public static void drawCustomImage(float v, float v1, int i, int i1, ResourceLocation resourceLocation, int i2) {
        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(new Color(i2).getRed() / 255f, new Color(i2).getGreen() / 255f, new Color(i2).getBlue() / 255f, new Color(i2).getAlpha() / 255f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        Gui.drawModalRectWithCustomSizedTexture((int) v, (int) v1, 0.0f, 0.0f, i, i1, (float) i, (float) i1);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void drawEntityESP(final double x, final double y, final double z, final double width,
                                     final double height, final float red, final float green, final float blue, final float alpha,
                                     final float lineRed, final float lineGreen, final float lineBlue, final float lineAlpha,
                                     final float lineWdith) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glLineWidth(lineWdith);
        GL11.glColor4f(lineRed, lineGreen, lineBlue, lineAlpha);
        drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawBoundingBox(final AxisAlignedBB aa) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawOutlinedBoundingBox(final AxisAlignedBB aa) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void pre3D() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    public static void post3D() {
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);
    }

    public static void prepareScissorBox(float x, float y, float x2, float y2) {
        ScaledResolution scale = new ScaledResolution(Minecraft.getMinecraft());
        int factor = scale.getScaleFactor();
        GL11.glScissor((int) ((int) (x * (float) factor)), (int) ((int) (((float) scale.getScaledHeight() - y2) * (float) factor)), (int) ((int) ((x2 - x) * (float) factor)), (int) ((int) ((y2 - y) * (float) factor)));
    }

    public static void drawRoundRect(float x, float y, float x1, float y1, int color) {
        drawRect(x, y, x1, y1, color);
//        drawBorderedRect(x, y, x1, y1, 4f, color, color);
//        GlStateManager.color(1,1,1);
    }

    public static void drawRoundedRect(float x, float y, float x2, float y2, final int round, final int color) {
        x += (float) (round / 2.0f + 0.5);
        y += (float) (round / 2.0f + 0.5);
        x2 -= (float) (round / 2.0f + 0.5);
        y2 -= (float) (round / 2.0f + 0.5);
        Gui.drawRect((int) x, (int) y, (int) x2, (int) y2, color);
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
        circle(x + round / 2.0f, y + round / 2.0f, round, color);
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
        Gui.drawRect((int) (x - round / 2.0f - 0.5f), (int) (y + round / 2.0f), (int) x2, (int) (y2 - round / 2.0f),
                color);
        Gui.drawRect((int) x, (int) (y + round / 2.0f), (int) (x2 + round / 2.0f + 0.5f), (int) (y2 - round / 2.0f),
                color);
        Gui.drawRect((int) (x + round / 2.0f), (int) (y - round / 2.0f - 0.5f), (int) (x2 - round / 2.0f),
                (int) (y2 - round / 2.0f), color);
        Gui.drawRect((int) (x + round / 2.0f), (int) y, (int) (x2 - round / 2.0f), (int) (y2 + round / 2.0f + 0.5f),
                color);
//        GL11.glDisable(2929);
//        GL11.glEnable(3042);
//        GL11.glDisable(3553);
//        GL11.glBlendFunc(770, 771);
//        GL11.glDepthMask(true);
//        GL11.glEnable(2848);
//        GL11.glHint(3154, 4354);
//        GL11.glHint(3155, 4354);
//        GL11.glScalef(0.5f, 0.5f, 0.5f);
//        drawVLine(n *= 2.0f, (n2 *= 2.0f) + 1.0f, (n4 *= 2.0f) - 2.0f, n5);
//        drawVLine((n3 *= 2.0f) - 1.0f, n2 + 1.0f, n4 - 2.0f, n5);
//        drawHLine(n + 2.0f, n3 - 3.0f, n2, n5);
//        drawHLine(n + 2.0f, n3 - 3.0f, n4 - 1.0f, n5);
//        drawHLine(n + 1.0f, n + 1.0f, n2 + 1.0f, n5);
//        drawHLine(n3 - 2.0f, n3 - 2.0f, n2 + 1.0f, n5);
//        drawHLine(n3 - 2.0f, n3 - 2.0f, n4 - 2.0f, n5);
//        drawHLine(n + 1.0f, n + 1.0f, n4 - 2.0f, n5);
//        drawRect(n + 1.0f, n2 + 1.0f, n3 - 1.0f, n4 - 1.0f, n6);
//        GL11.glScalef(2.0f, 2.0f, 2.0f);
//        GL11.glEnable(3553);
//        GL11.glDisable(3042);
//        GL11.glEnable(2929);
//        GL11.glDisable(2848);
//        GL11.glHint(3154, 4352);
//        GL11.glHint(3155, 4352);
    }

    public static void drawRoundedRectOnlyTop(float x, float y, float x2, float y2, final int round, final int color) {
        x += (float) (round / 2.0f + 0.5);
        y += (float) (round / 2.0f + 0.5);
        x2 -= (float) (round / 2.0f + 0.5);
        y2 -= (float) (round / 2.0f + 0.5);
        Gui.drawRect((int) x, (int) y, (int) x2, (int) y2, color);
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
        //circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
        circle(x + round / 2.0f, y + round / 2.0f, round, color);
        //circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
        Gui.drawRect((int) (x - round / 2.0f - 0.5f), (int) (y + round / 2.0f), (int) x2, (int) (y2 - round / 2.0f),
                color);
        Gui.drawRect((int) x, (int) (y + round / 2.0f), (int) (x2 + round / 2.0f + 0.5f), (int) (y2 - round / 2.0f),
                color);
        Gui.drawRect((int) (x + round / 2.0f), (int) (y - round / 2.0f - 0.5f), (int) (x2 - round / 2.0f),
                (int) (y2 - round / 2.0f), color);
        Gui.drawRect((int) (x + round / 2.0f), (int) y, (int) (x2 - round / 2.0f), (int) (y2 + round / 2.0f + 0.5f),
                color);
        // Middle rect
        Gui.drawRect((int)x - 2, (int)y + 5, (int)x2+2, (int)y2 + 2, color);
    }

    public static void drawVLine(float x, float y, float x1, int y1) {
        if (x1 < y) {
            float var5 = y;
            y = x1;
            x1 = var5;
        }
        drawRect(x, y + 1.0f, x + 1.0f, x1, y1);
    }

    public static void drawHLine(float x, float y, float x1, int y1) {
        if (y < x) {
            float var5 = x;
            x = y;
            y = var5;
        }
        drawRect(x, x1, y + 1.0f, x1 + 1.0f, y1);
    }
}