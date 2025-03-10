package com.github.dellixou.delclientv3.utils.gui.shaders.misc;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_FLAT;
import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_HEIGHT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WIDTH;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGetTexLevelParameteri;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glVertex2d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class DrawHelper{

    public static final HashMap<Integer, Integer> glowCache = new HashMap<Integer, Integer>();
    private static final Shader ROUNDED = new Shader("rounded.frag");
    private static final Shader ROUNDED_GRADIENT = new Shader("rounded_gradient.frag");
    private static final Shader ROUNDED_BLURRED = new Shader("rounded_blurred.frag");
    private static final Shader ROUNDED_ONLY_TOP = new Shader("rounded_only_top.frag");
    private static final Shader ROUNDED_ONLY_BOTTOM = new Shader("rounded_only_bottom.frag");
    private static final Shader ROUNDED_BLURRED_GRADIENT = new Shader("rounded_blurred_gradient.frag");
    private static final Shader ROUNDED_OUTLINE = new Shader("rounded_outline.frag");
    private static final Shader ROUNDED_TEXTURE = new Shader("rounded_texture.frag");
    public static final int STEPS = 60;
    public static final double ANGLE =  Math.PI * 2 / STEPS;
    public static final int EX_STEPS = 120;
    public static final double EX_ANGLE =  Math.PI * 2 / EX_STEPS;

    public enum Part {
        FIRST_QUARTER(4, Math.PI / 2),
        SECOND_QUARTER(4, Math.PI),
        THIRD_QUARTER(4, 3 * Math.PI / 2),
        FOURTH_QUARTER(4, 0d),
        FIRST_HALF(2, Math.PI / 2),
        SECOND_HALF(2, Math.PI),
        THIRD_HALF(2, 3 * Math.PI / 2),
        FOURTH_HALF(2, 0d);

        private int ratio;
        private double additionalAngle;

        private Part(int ratio, double addAngle) {
            this.ratio = ratio;
            this.additionalAngle = addAngle;
        }
    }

    public static void drawCircle(double x, double y, double radius, Color color) {
        drawSetup();
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        for(int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glLineWidth(1.5f);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_LOOP);
        for(int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    public static void drawCustomImage(float x, float y, float width, float height, ResourceLocation image) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GlStateManager.pushMatrix();

        // Determine target scale based on hover state
        float targetScale = 1f;

        // Interpolate scale using partialTicks
        //float interpolatedScale = currentScale + (targetScale - currentScale) * partialTicks;

        // Translate to the center of the image before rotating and scaling
        GlStateManager.translate(x + width / 2, y + height / 2, 0);
        GlStateManager.scale(1, 1, 1);
        GlStateManager.translate(-(x + width / 2), -(y + height / 2), 0);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0).tex(0, 1).endVertex();
        worldrenderer.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        worldrenderer.pos(x + width, y, 0).tex(1, 0).endVertex();
        worldrenderer.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

    // progress [1;100]
    // direction: 1 - 0
    public static void drawCircle(double x, double y, double radius, int progress, int direction, Color color) {
        double angle1 = direction == 0 ? ANGLE : -ANGLE;
        float steps = (STEPS / 100f) * progress;

        drawSetup();
        GlStateManager.disableCull();
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for(int i = 0; i <= steps; i++) {
            glVertex2d(x + radius * Math.sin(angle1 * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glLineWidth(1.5f);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_LOOP);
        glVertex2d(x, y);
        for(int i = 0; i <= steps; i++) {
            glVertex2d(x + radius * Math.sin(angle1 * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        GlStateManager.enableCull();
        drawFinish();
    }

    public static void drawCirclePart(double x, double y, double radius, Part part, Color color) {
        double angle = ANGLE / part.ratio;

        drawSetup();
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for(int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(part.additionalAngle + angle * i),
                    y + radius * Math.cos(part.additionalAngle + angle * i)
            );
        }
        glEnd();

        glLineWidth(1.5f);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_LOOP);
        glVertex2d(x, y);
        for(int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(part.additionalAngle + angle * i),
                    y + radius * Math.cos(part.additionalAngle + angle * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    public static void drawBlurredCircle(double x, double y, double radius, double blurRadius, Color color) {
        Color transparent = ColorHelper.injectAlpha(color, 0);

        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);
        glShadeModel(GL_SMOOTH);
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        for(int i = 0; i <= EX_STEPS; i++) {
            glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                    y + radius * Math.cos(EX_ANGLE * i)
            );
        }
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        for (int i = 0; i <= EX_STEPS + 1; i++) {
            if(i % 2 == 1) {
                applyColor(transparent);
                glVertex2d(x + (radius + blurRadius) * Math.sin(EX_ANGLE * i),
                        y + (radius + blurRadius) * Math.cos(EX_ANGLE * i));
            } else {
                applyColor(color);
                glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                        y + radius * Math.cos(EX_ANGLE * i));
            }
        }
        glEnd();

        glShadeModel(GL_FLAT);
        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public static void drawCircleOutline(double x, double y, double radius, float thikness, Color color) {
        drawSetup();
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(thikness);
        applyColor(color);

        glBegin(GL_LINE_LOOP);
        for(int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    // progress [1;100]
    // direction: 1 - по часовой стрелке; 0 - против часовой стрелки
    public static void drawCircleOutline(double x, double y, double radius, float thikness, int progress, int direction, Color color) {
        double angle1 = direction == 0 ? ANGLE : -ANGLE;
        float steps = (STEPS / 100f) * progress;

        drawSetup();
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(thikness);
        applyColor(color);

        glBegin(GL_LINE_STRIP);
        for(int i = 0; i <= steps; i++) {
            glVertex2d(x + radius * Math.sin(angle1 * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    public static void drawRainbowCircle(double x, double y, double radius, double blurRadius) {
        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);
        glShadeModel(GL_SMOOTH);
        applyColor(Color.WHITE);

        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for(int i = 0; i <= EX_STEPS; i++) {
            applyColor(Color.getHSBColor((float)i / EX_STEPS, 1f, 1f));
            glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                    y + radius * Math.cos(EX_ANGLE * i)
            );
        }
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        for(int i = 0; i <= EX_STEPS + 1; i++) {
            if(i % 2 == 1) {
                applyColor(ColorHelper.injectAlpha(Color.getHSBColor((float)i / EX_STEPS, 1f, 1f), 0));
                glVertex2d(x + (radius + blurRadius) * Math.sin(EX_ANGLE * i),
                        y + (radius + blurRadius) * Math.cos(EX_ANGLE * i));
            } else {
                applyColor(Color.getHSBColor((float)i / EX_STEPS, 1f, 1f));
                glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                        y + radius * Math.cos(EX_ANGLE * i));
            }
        }
        glEnd();

        glShadeModel(GL_FLAT);
        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public static void drawRect(double x, double y, double width, double height, Color color) {
        drawSetup();
        applyColor(color);

        glBegin(GL_QUADS);
        glVertex2d(x, y);
        glVertex2d(x + width, y);
        glVertex2d(x + width, y - height);
        glVertex2d(x, y - height);
        glEnd();

        drawFinish();
    }

    public static void drawGradientRect(double x, double y, double width, double height, Color... clrs) {
        drawSetup();
        glShadeModel(GL_SMOOTH);

        glBegin(GL_QUADS);
        applyColor(clrs[1]);
        glVertex2d(x, y);
        applyColor(clrs[2]);
        glVertex2d(x + width, y);
        applyColor(clrs[3]);
        glVertex2d(x + width, y - height);
        applyColor(clrs[0]);
        glVertex2d(x, y - height);
        glEnd();

        glShadeModel(GL_FLAT);
        drawFinish();
    }

    public static void drawRoundedRect(double x, double y, double width, double height, double radius, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        drawSetup();

        ROUNDED.load();
        ROUNDED.setUniformf("size", (float)width * 2, (float)height * 2);
        ROUNDED.setUniformf("round", (float)radius * 2);
        ROUNDED.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw(x, y - height, width, height);
        ROUNDED.unload();

        drawFinish();
    }

    public static void drawRoundedOnlyTop(double x, double y, double width, double height, double radius, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        drawSetup();

        ROUNDED_ONLY_TOP.load();
        ROUNDED_ONLY_TOP.setUniformf("size", (float)width * 2, (float)height * 2);
        ROUNDED_ONLY_TOP.setUniformf("round", (float)radius * 2);
        ROUNDED_ONLY_TOP.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw(x, y, width, height);
        ROUNDED_ONLY_TOP.unload();

        drawFinish();
    }

    public static void drawRoundedOnlyBottom(double x, double y, double width, double height, double radius, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        drawSetup();

        ROUNDED_ONLY_BOTTOM.load();
        ROUNDED_ONLY_BOTTOM.setUniformf("size", (float)width * 2, (float)height * 2);
        ROUNDED_ONLY_BOTTOM.setUniformf("round", (float)radius * 2);
        ROUNDED_ONLY_BOTTOM.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw(x, y, width, height);
        ROUNDED_ONLY_BOTTOM.unload();

        drawFinish();
    }

    public static void drawRoundedGradientRect(double x, double y, double width, double height, double radius, Color... colors) {
        float[] c = ColorHelper.getColorComps(colors[0]);
        float[] c1 = ColorHelper.getColorComps(colors[1]);
        float[] c2 = ColorHelper.getColorComps(colors[2]);
        float[] c3 = ColorHelper.getColorComps(colors[3]);

        drawSetup();

        ROUNDED_GRADIENT.load();
        ROUNDED_GRADIENT.setUniformf("size", (float)width * 2, (float)height * 2);
        ROUNDED_GRADIENT.setUniformf("round", (float)radius * 2);
        ROUNDED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3]);
        ROUNDED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3]);
        ROUNDED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3]);
        ROUNDED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3]);
        Shader.draw(x, y - height, width, height);
        ROUNDED_GRADIENT.unload();

        drawFinish();
    }

    public static void drawRoundedBlurredRect(double x, double y, double width, double height, double roundR, float blurR, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);

        ROUNDED_BLURRED.load();
        ROUNDED_BLURRED.setUniformf("size", (float)(width + 2 * blurR), (float)(height + 2 * blurR));
        ROUNDED_BLURRED.setUniformf("softness", blurR);
        ROUNDED_BLURRED.setUniformf("radius", (float)roundR);
        ROUNDED_BLURRED.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw(x - blurR, y - height - blurR, width + blurR * 2, height + blurR * 2);
        ROUNDED_BLURRED.unload();

        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public static void drawRoundedGradientBlurredRect(double x, double y, double width, double height, double roundR, float blurR, Color... colors) {
        float[] c = ColorHelper.getColorComps(colors[0]);
        float[] c1 = ColorHelper.getColorComps(colors[1]);
        float[] c2 = ColorHelper.getColorComps(colors[2]);
        float[] c3 = ColorHelper.getColorComps(colors[3]);

        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);

        ROUNDED_BLURRED_GRADIENT.load();
        ROUNDED_BLURRED_GRADIENT.setUniformf("size", (float)(width + 2 * blurR), (float)(height + 2 * blurR));
        ROUNDED_BLURRED_GRADIENT.setUniformf("softness", blurR);
        ROUNDED_BLURRED_GRADIENT.setUniformf("radius", (float)roundR);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3]);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3]);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3]);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3]);
        Shader.draw(x - blurR, y - height - blurR, width + blurR * 2, height + blurR * 2);
        ROUNDED_BLURRED_GRADIENT.unload();

        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public static void drawSmoothRect(double x, double y, double width, double height, Color color) {
        drawRoundedRect(x, y, width, height, 1.5, color);
    }

    public static void drawRoundedRectOutline(double x, double y, double width, double height, double radius, float thickness, Color color) {
        float[] c = ColorHelper.getColorComps(color);

        drawSetup();

        ROUNDED_OUTLINE.load();
        ROUNDED_OUTLINE.setUniformf("size", (float)width * 2, (float)height * 2);
        ROUNDED_OUTLINE.setUniformf("round", (float)radius * 2);
        ROUNDED_OUTLINE.setUniformf("thickness", thickness);
        ROUNDED_OUTLINE.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw(x, y - height, width, height);
        ROUNDED_OUTLINE.unload();

        drawFinish();
    }

    public static void drawTexture(ResourceLocation identifier, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
        drawTexture(Utils.getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight);
    }

    public static void drawTexture(int texId, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        resetColor();

        GlStateManager.bindTexture(texId);

        int iWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int iHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        y -= height;
        texX = texX / iWidth;
        texY = texY / iHeight;
        texWidth = texWidth / iWidth;
        texHeight = texHeight / iHeight;

        glBegin(GL_QUADS);
        glTexCoord2d(texX, texY);
        glVertex2d(x, y);
        glTexCoord2d(texX, texY + texHeight);
        glVertex2d(x, y + height);
        glTexCoord2d(texX + texWidth, texY + texHeight);
        glVertex2d(x + width, y + height);
        glTexCoord2d(texX + texWidth, texY);
        glVertex2d(x + width, y);
        glEnd();

        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }

    public static void drawRoundedTexture(ResourceLocation identifier, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight, double radius) {
        drawRoundedTexture(Utils.getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight, radius);
    }

    public static void drawRoundedTexture(int texId, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight, double radius) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.3f);

        //Minecraft.getMinecraft().getMainRenderTarget().bindWrite(false);
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
        Utils.initStencilReplace();
        drawRoundedRect(x, y, width, height, radius, Color.WHITE);
        Utils.uninitStencilReplace();

        GlStateManager.bindTexture(texId);

        int iWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int iHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        y -= height;
        texX = texX / iWidth;
        texY = texY / iHeight;
        texWidth = texWidth / iWidth;
        texHeight = texHeight / iHeight;

        glBegin(GL_QUADS);
        glTexCoord2d(texX, texY);
        glVertex2d(x, y);
        glTexCoord2d(texX, texY + texHeight);
        glVertex2d(x, y + height);
        glTexCoord2d(texX + texWidth, texY + texHeight);
        glVertex2d(x + width, y + height);
        glTexCoord2d(texX + texWidth, texY);
        glVertex2d(x + width, y);
        glEnd();

        GlStateManager.bindTexture(0);
        glDisable(GL_STENCIL_TEST);
        glDisable(GL_ALPHA_TEST);
        GlStateManager.disableBlend();
    }

    public static void drawRoundedTexture(ResourceLocation identifier, double x, double y, double width, double height, double radius, Color color) {
        drawRoundedTexture(Utils.getTextureId(identifier), x, y, width, height, radius, color);
    }

    public static void drawRoundedTexture(int texId, double x, double y, double width, double height, double radius, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Définir la couleur
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        ROUNDED_TEXTURE.load();
        ROUNDED_TEXTURE.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_TEXTURE.setUniformf("round", (float) radius * 2);
        ROUNDED_TEXTURE.setUniformf("color", r, g, b, a); // Passe la couleur au shader

        GlStateManager.bindTexture(texId);
        Shader.draw(x, y - height, width, height);
        GlStateManager.bindTexture(0);

        ROUNDED_TEXTURE.unload();

        GlStateManager.color(1, 1, 1, 1); // Réinitialiser la couleur après le dessin
        GlStateManager.disableBlend();
        resetColor();
    }

    public static void drawSmoothIcon(ResourceLocation icon, float x, float y, int width, int height, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableAlpha();

        Minecraft.getMinecraft().getTextureManager().bindTexture(icon);

        // Appliquer la couleur
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();

        // Réinitialiser la couleur
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawIcon(ResourceLocation icon, int x, int y, int width, int height) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Minecraft.getMinecraft().getTextureManager().bindTexture(icon);

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableTexture2D();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        float scaleX = (float)width / 512;
        float scaleY = (float)height / 512;

        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scaleX, scaleY, 1.0F);

        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 512, 512, 512, 512);

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.popMatrix();
    }


    public static void drawGlow(double x, double y, int width, int height, int glowRadius, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);

        GlStateManager.bindTexture(getGlowTexture(width, height, glowRadius));
        width += glowRadius * 2;
        height += glowRadius * 2;
        x -= glowRadius;
        y -= height - glowRadius;

        applyColor(color);
        glBegin(GL_QUADS);
        glTexCoord2d(0, 1);
        glVertex2d(x, y);
        glTexCoord2d(0, 0);
        glVertex2d(x, y + height);
        glTexCoord2d(1, 0);
        glVertex2d(x + width, y + height);
        glTexCoord2d(1, 1);
        glVertex2d(x + width, y);
        glEnd();

        GlStateManager.bindTexture(0);
        glDisable(GL_ALPHA_TEST);
        GlStateManager.disableBlend();
    }

    public static int getGlowTexture(int width, int height, int blurRadius) {
        int identifier = (width * 401 + height) * 407 + blurRadius;
        int texId = glowCache.getOrDefault(identifier, -1);

        if(texId == -1) {
            BufferedImage original = new BufferedImage((int)(width + blurRadius * 2), (int)(height + blurRadius * 2), BufferedImage.TYPE_INT_ARGB_PRE);

            Graphics g = original.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(blurRadius, blurRadius, (int)width, (int)height);
            g.dispose();

            GlowFilter glow = new GlowFilter(blurRadius);
            BufferedImage blurred = glow.filter(original, null);
            try {
                texId = Utils.loadTexture(blurred);
                glowCache.put(identifier, texId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return texId;
    }

    // для scaledHeight, scale - юзайте WINDOW.getGuiScaledHeight(), WINDOW.getGuiScale() если сами не меняли их
    public static void scissor(double x, double y, double width, double height, double scale, double scaledHeight) {
        glScissor((int)(x * scale),
                (int)((scaledHeight - y) * scale),
                (int)(width * scale),
                (int)(height * scale));
    }

    public static void applyColor(Color color) {
        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public static void resetColor() {
        glColor4f(1f, 1f, 1f, 1f);
    }

    public static void enableScissor() {
        glEnable(GL_SCISSOR_TEST);
    }

    public static void disableScissor() {
        glDisable(GL_SCISSOR_TEST);
    }

    public static void drawSetup() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void drawFinish() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        resetColor();
    }



}