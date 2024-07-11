package com.github.dellixou.delclientv3.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Applique un scissor (découpage) à une zone spécifique de l'écran.
     *
     * @param x La coordonnée X du coin supérieur gauche de la zone de découpage
     * @param y La coordonnée Y du coin supérieur gauche de la zone de découpage
     * @param width La largeur de la zone de découpage
     * @param height La hauteur de la zone de découpage
     */
    public static void scissor(float x, float y, float width, float height) {
        ScaledResolution sr = new ScaledResolution(mc);
        int scaleFactor = sr.getScaleFactor();

        GL11.glScissor((int)(x * scaleFactor),
                (int)(mc.displayHeight - (y + height) * scaleFactor),
                (int)(width * scaleFactor),
                (int)(height * scaleFactor));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    /**
     * Désactive le scissor test.
     */
    public static void endScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
