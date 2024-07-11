package com.github.dellixou.delclientv3.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * @author TerrificTable
 * @since 03/26/23
 */
public class Shader {
    protected final int programID;
    private final int fragmentID;
    private final int vertexID;

    private long startTime;

    /**
     * Shader constructor
     *
     * @param fragmentShader location of fragment shader
     * @param vertexShader location of vertex shader
     */
    public Shader(String fragmentShader, String vertexShader) {
        int program = glCreateProgram();

        fragmentID = createShader(readShader("/assets/minecraft/" + fragmentShader), GL_FRAGMENT_SHADER);
        glAttachShader(program, fragmentID);

        vertexID = createShader(readShader("/assets/minecraft/" + vertexShader), GL_VERTEX_SHADER);
        glAttachShader(program, vertexID);

        glLinkProgram(program);
        int state = glGetProgrami(program, GL_LINK_STATUS);
        if (state == 0) {
            throw new IllegalStateException(String.format("Failed to link program/shaders with error: %s", glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH))));
        }

        this.programID = program; // only change programID when program is shaders are compiled and program is linked successfully
        startTime = System.currentTimeMillis();
    }
    public Shader(String fragmentShader) {
        this(fragmentShader, "shaders/vertex.vsh");
    }

    /**
     * small Utility for creating shaders because I cant be bothered to write this code twice
     */
    private int createShader(String shader, int type) {
        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shader);
        glCompileShader(shaderID);

        int state = glGetShaderi(shaderID, GL_COMPILE_STATUS);
        if (state == 0) {
            System.err.println(glGetShaderInfoLog(shaderID, glGetShaderi(shaderID, GL_INFO_LOG_LENGTH)));
            throw new IllegalStateException("Failed to compile shader of type: " + type);
        }

        return shaderID;
    }

    /**
     * Use shader wtih default uniforms (time and resolution)
     */
    public void use() throws IllegalArgumentException {
        if (!glIsProgram(programID)) {
            throw new IllegalArgumentException("Shader has been deleted");
        }

        glUseProgram(programID);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        // default uniforms for every shader
        glUniform1f(glGetUniformLocation(programID, "time"), (System.currentTimeMillis() - startTime) / 1000.0f);
        glUniform2f(glGetUniformLocation(programID, "resolution"), sr.getScaledWidth(), sr.getScaledHeight());
    }

    /**
     * Use shader with custom uniforms (and default uniforms, time and resolution)
     *
     * @param uniforms Customer of integer, integer is programID
     */
    public void use(Consumer<Integer> uniforms) {
        use();
        uniforms.accept(programID);
    }

    /**
     * Stops using this shader
     */
    public void finish() {
        glUseProgram(0);
    }

    /**
     * Deletes this shader
     */
    public void delete() {
        glDetachShader(programID, fragmentID);
        glDetachShader(programID, vertexID);

        glDeleteProgram(programID);
        glDeleteShader(fragmentID);
        glDeleteShader(vertexID);
    }

    /**
     * Read the shader file
     *
     * @param shaderLoc location of shader
     * @return content of shader file as string
     */
    private String readShader(String shaderLoc) {
        InputStream file = Shader.class.getResourceAsStream(shaderLoc);
        if (file == null) {
            throw new IllegalArgumentException("Read Shader: 'file' cannot be null, shaderLoc: " + shaderLoc);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(file));
        StringBuilder result = new StringBuilder();
        reader.lines().forEach(line -> result.append(line).append("\n"));

        return result.toString();
    }

    public void drawQuads(float x, float y, float width, float height) {
        glBegin(GL_QUADS);
        glTexCoord2d(0, 0);
        glVertex2d(x, y);
        glTexCoord2d(0, 1);
        glVertex2d(x, y + height);
        glTexCoord2d(1, 1);
        glVertex2d(x + width, y + height);
        glTexCoord2d(1, 0);
        glVertex2d(x + width, y);
        glEnd();
    }

    public void drawQuads() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        drawQuads(0, 0, sr.getScaledWidth(), sr.getScaledHeight());
    }

    public int getProgramID() {
        return programID;
    }
}