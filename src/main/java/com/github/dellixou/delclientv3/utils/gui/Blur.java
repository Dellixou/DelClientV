package com.github.dellixou.delclientv3.utils.gui;

import com.google.common.annotations.Beta;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * @author TerrificTable
 * @since Mo, 27/03/23 17:54
 */
@SuppressWarnings({"unused"})
public class Blur {
    private static Blur instance;

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Shader blurShader = new Shader("shaders/gaussian_rounded.frag"); // shader for (screen) blur
    private Framebuffer framebuffer = new Framebuffer(1, 1, false); // the framebuffer the blur is rendered to

    private FloatBuffer weightBuffer; // buffer that stores weights
    private float oldRadius = -1; // used to check if weights have to be recalculated


    // make constructor private
    private Blur() {}


    /**
     * Get Blur instance
     *
     * @return this, the Blur instance
     */
    public static Blur getInstance() {
        if (instance == null)
            instance = new Blur();
        return instance;
    }



    /**
     * Calculate weights for this radius
     * @param radius the radius to calculate weights for
     */
    private void calculateWeights(float radius) {
        if (radius != oldRadius) {
            weightBuffer = BufferUtils.createFloatBuffer(256);
            for (int i = 0; i <= radius; i++) {
                weightBuffer.put(calculateGaussianValue(i, radius / 2));
            }

            weightBuffer.rewind();
        }
        oldRadius = radius;
    }

    /**
     * set shader uniforms
     *
     * @param horiz blur horizontally
     * @param vert blur vertically
     * @param radius blur radius
     * @param pid shader program id
     */
    private void setupUniforms(int horiz, int vert, float radius, int pid, float width, float height, float round) {
        glUniform1i(glGetUniformLocation(pid, "textureIn"), 0);
        glUniform2f(glGetUniformLocation(pid, "texelSize"), 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        glUniform2f(glGetUniformLocation(pid, "direction"), horiz, vert);
        glUniform1f(glGetUniformLocation(pid, "radius"), radius);
        glUniform2f(glGetUniformLocation(pid, "size"), width, height);
        glUniform1f(glGetUniformLocation(pid, "round"), round);

        calculateWeights(radius);
        glUniform1(glGetUniformLocation(pid, "weights"), weightBuffer);
    }



    /**
     * setup uniforms for rounded Blur
     *
     * @param x x position of blr
     * @param y y position of blur
     * @param width width of blur
     * @param height height of blur
     * @param softness edge softness (can basically be ignored)
     * @param horiz blur horizontally (1/0 and has to be opposite of vert)
     * @param vert blur vertically (1/0 and has to be opposite of horiz)
     * @param blurRadius blur radius
     * @param pid programID of shader
     */
    private void setupRoundUniforms(float x, float y, float width, float height, float softness, int horiz, int vert, float blurRadius, int pid) {
        /* GAUSSIAN BLUR */
        glUniform1i(glGetUniformLocation(pid, "textureIn"), 0);
        glUniform2f(glGetUniformLocation(pid, "texelSize"), 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        glUniform2f(glGetUniformLocation(pid, "direction"), horiz, vert);
        glUniform1f(glGetUniformLocation(pid, "blurRadius"), blurRadius);

        calculateWeights(blurRadius);
        glUniform1(glGetUniformLocation(pid, "weights"), weightBuffer);
    }

    /**
     * Render blur over the entire screen (overload for {@code renderBlurScreen})
     *
     * @param blurRadius the blur radius
     */
    public void renderBlurScreen(float blurRadius, float width, float height, float roundRadius) {
        renderBlurSection(0, 0, mc.displayWidth, mc.displayHeight, blurRadius, width, height, roundRadius);
    }


    /**
     * Renders blur over box
     *
     * @param x x position of blur box
     * @param y y position of blur box
     * @param width width of blur box
     * @param height height of blur box
     * @param blurRadius the blur radius
     */
    public void renderBlurSection(float x, float y, float width, float height, float blurRadius, float rectWidth, float rectHeight, float roundRadius) {
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        framebuffer = createFrameBuffer(framebuffer, mc.displayWidth, mc.displayHeight);
        framebuffer.framebufferClear();

        framebuffer.bindFramebuffer(true);
        blurShader.use(pid -> setupUniforms(1, 0, blurRadius, pid, rectWidth, rectHeight, roundRadius));
        glBindTexture(GL_TEXTURE_2D, mc.getFramebuffer().framebufferTexture);
        blurShader.drawQuads();
        framebuffer.unbindFramebuffer();
        blurShader.finish();

        glEnable(GL_SCISSOR_TEST);
        RenderUtils.scissor(x, y, width, height);
        mc.getFramebuffer().bindFramebuffer(true);
        blurShader.use(pid -> setupUniforms(0, 1, blurRadius, pid, rectWidth, rectHeight, roundRadius));
        glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture);
        blurShader.drawQuads();
        blurShader.finish();
        RenderUtils.endScissor();

        glColor4f(1, 1, 1, 1);
        GlStateManager.bindTexture(0);
    }



    public void renderBlurSectionWithRoundedCorners(float x, float y, float width, float height, float blurRadius, float roundRadius) {
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        framebuffer = createFrameBuffer(framebuffer, mc.displayWidth, mc.displayHeight);
        framebuffer.framebufferClear();

        framebuffer.bindFramebuffer(true);
        blurShader.use(pid -> setupUniforms(1, 0, blurRadius, pid, width, height, roundRadius));
        glBindTexture(GL_TEXTURE_2D, mc.getFramebuffer().framebufferTexture);
        blurShader.drawQuads();
        framebuffer.unbindFramebuffer();
        blurShader.finish();

        glEnable(GL_SCISSOR_TEST);
        RenderUtils.scissor(x, y, width, height);
        mc.getFramebuffer().bindFramebuffer(true);
        blurShader.use(pid -> setupUniforms(0, 1, blurRadius, pid, width, height, roundRadius));
        glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture);
        blurShader.drawQuads();
        blurShader.finish();
        RenderUtils.endScissor();

        glColor4f(1, 1, 1, 1);
        GlStateManager.bindTexture(0);
    }

    /**
     * calculates gaussian value for blur
     * @see <a href="https://en.wikipedia.org/wiki/Gaussian_function">Gaussian function</a>
     * @see <a href="https://en.wikipedia.org/wiki/Gaussian_blur">Gaussian blur</a>
     *
     * @param x is value passed to gaussian function
     * @param sigma horizontal/vertical distance
     * @return output of gaussian function
     */
    private float calculateGaussianValue(int x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    /**
     * utility function to create/update buffer when screen size changes
     *
     * @param framebuffer the buffer to be updated
     * @return new buffer
     */
    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, float width, float height) {
        if (framebuffer == null || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer((int) width, (int) height, true);
        }
        return framebuffer;
    }
}
