package com.github.dellixou.delclientv3.utils.gui.misc;

import java.util.Queue;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Queues;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;

public class BloomHelper{
    private static final Shader bloom = new Shader("bloom.frag");
    private static final Queue<Runnable> renderQueue = Queues.newConcurrentLinkedQueue();
    private static Framebuffer inFrameBuffer;
    private static Framebuffer outFrameBuffer;

    public static void registerRenderCall(Runnable rc) {
        renderQueue.add(rc);
    }

    public static void draw(int radius) {
        if(renderQueue.isEmpty())
            return;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();

        inFrameBuffer = setupBuffer(inFrameBuffer, width, height);
        outFrameBuffer = setupBuffer(outFrameBuffer, width, height);

        inFrameBuffer.bindFramebuffer(true);

        while(!renderQueue.isEmpty()) {
            renderQueue.poll().run();
        }

        outFrameBuffer.bindFramebuffer(true);

        bloom.load();
        bloom.setUniformf("radius", radius);
        bloom.setUniformi("sampler1", 0);
        bloom.setUniformi("sampler2", 20);
        bloom.setUniformfb("kernel", Utils.getKernel(radius));
        bloom.setUniformf("texelSize", 1.0F / (float)width, 1.0F / (float)height);
        bloom.setUniformf("direction", 2.0F, 0.0F);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0001f);

        inFrameBuffer.bindFramebufferTexture();
        Shader.draw();

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bloom.setUniformf("direction", 0.0F, 2.0F);

        outFrameBuffer.bindFramebufferTexture();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit + 20);
        inFrameBuffer.bindFramebufferTexture();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        Shader.draw();

        bloom.unload();
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }

    private static Framebuffer setupBuffer(Framebuffer frameBuffer, int width, int height) {
        if(frameBuffer == null || frameBuffer.framebufferWidth != width || frameBuffer.framebufferHeight != height)
            frameBuffer = new Framebuffer(width, height, true);
        else
            frameBuffer.framebufferClear();
        frameBuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);

        return frameBuffer;
    }
}
