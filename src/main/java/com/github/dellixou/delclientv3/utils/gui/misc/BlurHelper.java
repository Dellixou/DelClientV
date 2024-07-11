package com.github.dellixou.delclientv3.utils.gui.misc;

import java.util.Queue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Queues;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;

public class BlurHelper {
    private static final Shader blur = new Shader("blur.frag");
    private static final Queue<Runnable> renderQueue = Queues.newConcurrentLinkedQueue();
    private static Framebuffer inFrameBuffer;
    private static Framebuffer outFrameBuffer;

    /**
     * Enregistre un appel de rendu à exécuter lors de l'effet de flou.
     *
     * @param rc L'appel de rendu à enregistrer.
     */
    public static void registerRenderCall(Runnable rc) {
        renderQueue.add(rc);
    }

    /**
     * Dessine l'effet de flou avec le rayon spécifié.
     *
     * @param radius Le rayon de l'effet de flou.
     */
    public static void draw(int radius) {
        if (renderQueue.isEmpty())
            return;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int scaleFactor = sr.getScaleFactor();
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        inFrameBuffer = setupBuffer(inFrameBuffer, width, height);
        outFrameBuffer = setupBuffer(outFrameBuffer, width, height);

        // Lier le framebuffer d'entrée pour le rendu
        inFrameBuffer.bindFramebuffer(true);

        // Exécuter tous les appels de rendu
        while (!renderQueue.isEmpty()) {
            renderQueue.poll().run();
        }

        // Lier le framebuffer de sortie pour l'effet de flou
        outFrameBuffer.bindFramebuffer(true);

        // Charger le shader de flou et définir les uniformes
        blur.load();
        blur.setUniformf("radius", radius);
        blur.setUniformi("sampler1", 0);
        blur.setUniformi("sampler2", 20);
        blur.setUniformfb("kernel", Utils.getKernel(radius));
        blur.setUniformf("texelSize", 1.0F / width, 1.0F / height);
        blur.setUniformf("direction", 2.0F, 0.0F);

        // Effectuer le passage de flou horizontal
        GlStateManager.disableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        mc.getFramebuffer().bindFramebufferTexture();
        Shader.draw();

        // Effectuer le passage de flou vertical
        mc.getFramebuffer().bindFramebuffer(true);
        blur.setUniformf("direction", 0.0F, 2.0F);
        outFrameBuffer.bindFramebufferTexture();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit + 20);
        inFrameBuffer.bindFramebufferTexture();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        Shader.draw();

        // Décharger le shader et réinitialiser les états OpenGL
        blur.unload();
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }

    /**
     * Configure un framebuffer avec la largeur et la hauteur spécifiées.
     *
     * @param frameBuffer Le framebuffer à configurer.
     * @param width       La largeur du framebuffer.
     * @param height      La hauteur du framebuffer.
     * @return Le framebuffer configuré.
     */
    private static Framebuffer setupBuffer(Framebuffer frameBuffer, int width, int height) {
        if (frameBuffer == null || frameBuffer.framebufferWidth != width || frameBuffer.framebufferHeight != height)
            return new Framebuffer(width, height, true);
        else {
            frameBuffer.framebufferClear();
            return frameBuffer;
        }
    }
}
