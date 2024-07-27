package com.github.dellixou.delclientv3.utils.gui.shaders.misc;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.stream.Collectors;


public class Shader{

    public static final int VERTEX_SHADER;
    private int programId;

    static {
        VERTEX_SHADER = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(VERTEX_SHADER, getShaderSource("vertex.vert"));
        glCompileShader(VERTEX_SHADER);
    }

    public Shader(String fragmentShaderName) {
        int programId = glCreateProgram();
        try {
            int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fragmentShader, getShaderSource(fragmentShaderName));
            glCompileShader(fragmentShader);

            int isFragmentCompiled = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
            if(isFragmentCompiled == 0) {
                glDeleteShader(fragmentShader);
                System.err.println("Fragment shader couldn't compile. It has been deleted.");
            }

            glAttachShader(programId, VERTEX_SHADER);
            glAttachShader(programId, fragmentShader);
            glLinkProgram(programId);
        } catch(Exception e) {
            e.printStackTrace();
        }
        this.programId = programId;
    }

    public void load() {
        glUseProgram(programId);
    }

    public void unload() {
        glUseProgram(0);
    }

    public int getUniform(String name) {
        return glGetUniformLocation(programId, name);
    }

    public void setUniformf(String name, float... args) {
        int loc = glGetUniformLocation(programId, name);
        switch (args.length) {
            case 1:
                glUniform1f(loc, args[0]);
                break;
            case 2:
                glUniform2f(loc, args[0], args[1]);
                break;
            case 3:
                glUniform3f(loc, args[0], args[1], args[2]);
                break;
            case 4:
                glUniform4f(loc, args[0], args[1], args[2], args[3]);
                break;
        }
    }

    public void setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(programId, name);
        switch (args.length) {
            case 1:
                glUniform1i(loc, args[0]);
                break;
            case 2:
                glUniform2i(loc, args[0], args[1]);
                break;
            case 3:
                glUniform3i(loc, args[0], args[1], args[2]);
                break;
            case 4:
                glUniform4i(loc, args[0], args[1], args[2], args[3]);
                break;
        }
    }

    public void setUniformfb(String name, FloatBuffer buffer) {
        glUniform1(glGetUniformLocation(programId, name), buffer);
    }

    public static void draw() {
        draw(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    }

    public static void draw(double x, double y, double width, double height) {
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

    public static String getShaderSource(String fileName) {
        String source = "";
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(DelClient.class.getResourceAsStream("/assets/minecraft/shaders/" + fileName)))) {
            source = bufferedReader.lines().filter(str -> !str.isEmpty()).map(str -> str.replace("\t", "")).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source;
    }

}