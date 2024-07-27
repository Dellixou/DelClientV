package com.github.dellixou.delclientv3.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageProcessor {
    public static ResourceLocation resizeAndLoadTexture(ResourceLocation originalTexture, int targetWidth, int targetHeight) {
        try {
            // Charger l'image originale
            BufferedImage originalImage = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(originalTexture).getInputStream());

            // Redimensionner l'image
            BufferedImage resizedImage = resizeImage(originalImage, targetWidth, targetHeight);

            // Créer une nouvelle texture dynamique
            DynamicTexture dynamicTexture = new DynamicTexture(resizedImage);

            // Créer une nouvelle ResourceLocation pour la texture redimensionnée
            return Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("resized_" + originalTexture.getResourcePath(), dynamicTexture);
        } catch (IOException e) {
            e.printStackTrace();
            return originalTexture; // Retourner la texture originale en cas d'erreur
        }
    }

    // La méthode resizeImage reste la même que dans votre code original
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }
}
