package com.github.dellixou.delclientv3.utils.gui.shaders.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class ShaderHelper {
    private static int blurShaderProgram;

    public static void init() {
        blurShaderProgram = createShader("/assets/minecraft/shaders/custom_blur.frag");
        System.out.println("Shader program created with ID: " + blurShaderProgram);

        int numUniforms = GL20.glGetProgrami(blurShaderProgram, GL20.GL_ACTIVE_UNIFORMS);
        for (int i = 0; i < numUniforms; i++) {
            String name = GL20.glGetActiveUniform(blurShaderProgram, i, 256);
            int location = GL20.glGetUniformLocation(blurShaderProgram, name);
            System.out.println("Uniform " + i + ": " + name + " at location " + location);
        }
    }

    private static int createShader(String fragShaderFile) {
        int program = GL20.glCreateProgram();
        int fragShader = createShaderFromFile(fragShaderFile, GL20.GL_FRAGMENT_SHADER);

        GL20.glAttachShader(program, fragShader);
        GL20.glLinkProgram(program);

        int linked = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        if (linked == 0) {
            System.err.println("Shader program linking failed: " + GL20.glGetProgramInfoLog(program, 1024));
            throw new RuntimeException("Failed to link shader program");
        }

        System.out.println("Shader program created successfully with ID: " + program);
        System.out.println("Active attributes: " + GL20.glGetProgrami(program, GL20.GL_ACTIVE_ATTRIBUTES));
        System.out.println("Active uniforms: " + GL20.glGetProgrami(program, GL20.GL_ACTIVE_UNIFORMS));

        return program;
    }

    private static int createShaderFromFile(String filename, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        String shaderCode = readFileAsString(filename);
        System.out.println("Shader code:\n" + shaderCode);
        GL20.glShaderSource(shader, shaderCode);
        GL20.glCompileShader(shader);

        int compiled = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
        if (compiled == 0) {
            System.err.println("Shader compilation failed: " + GL20.glGetShaderInfoLog(shader, 1024));
            throw new RuntimeException("Failed to compile shader");
        }

        return shader;
    }

    private static String readFileAsString(String filename) {
        try (InputStream inputStream = ShaderHelper.class.getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read shader file", e);
        }
    }

    public static void renderBlurredRect(float x, float y, float width, float height) {
        Minecraft mc = Minecraft.getMinecraft();
        Framebuffer framebuffer = mc.getFramebuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();

        framebuffer.bindFramebuffer(false);

        GL20.glUseProgram(blurShaderProgram);

        int gcolorLocation = GL20.glGetUniformLocation(blurShaderProgram, "gcolor");
        int viewHeightLocation = GL20.glGetUniformLocation(blurShaderProgram, "viewHeight");

        System.out.println("Uniform locations in render - gcolor: " + gcolorLocation + ", viewHeight: " + viewHeightLocation);

        if (gcolorLocation == -1 || viewHeightLocation == -1) {
            System.err.println("Error: Uniform location not found. gcolor: " + gcolorLocation + ", viewHeight: " + viewHeightLocation);
            GL20.glUseProgram(0);
            GlStateManager.popMatrix();
            return;
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        GL20.glUniform1i(gcolorLocation, 0);
        GL20.glUniform1f(viewHeightLocation, (float) mc.displayHeight);

        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getWorldRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        tessellator.getWorldRenderer().pos(x, y + height, 0).tex(0, 1).endVertex();
        tessellator.getWorldRenderer().pos(x + width, y + height, 0).tex(1, 1).endVertex();
        tessellator.getWorldRenderer().pos(x + width, y, 0).tex(1, 0).endVertex();
        tessellator.getWorldRenderer().pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();

        GL20.glUseProgram(0);
        framebuffer.unbindFramebuffer();

        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private static void checkGLError(String step) {
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            System.err.println("OpenGL error " + error + " at step: " + step);
        }
    }
}
