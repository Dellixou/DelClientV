package com.github.dellixou.delclientv3.utils.renderer;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.utils.gui.DrawingUtils;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class WorldTextRenderer {

    private static GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 40, true, true, true);

    public static void renderTextInWorld(Minecraft mc, String text, double x, double y, double z, float partialTicks, float scale, boolean bg) {
        Vec3 playerPos = mc.thePlayer.getPositionEyes(partialTicks);
        double distance = playerPos.distanceTo(new Vec3(x, y, z));
        float maxDistance = 20.0f;
        float minDistance = 8.0f;

        // Calculate opacity
        float opacity = 1.0f;

        // If the distance is greater than maxDistance, do not render the text
        if (distance >= maxDistance-1) {
            opacity = 0.001f;
            return;
        }

        if (distance > minDistance) {
            opacity = 1.001f - (float) (distance - minDistance) / (maxDistance - minDistance);
        }


        // Ensure opacity is within the correct bounds
        opacity = Math.max(0.0f, Math.min(1.0f, opacity));

        boolean renderWall = DelClient.settingsManager.getSettingById("user_route_render_wall").getValBoolean();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - playerPos.xCoord, y - playerPos.yCoord, z - playerPos.zCoord);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        if (renderWall) {
            GL11.glDisable(GL11.GL_DEPTH_TEST); // Draw the text on top of the geometry
        }
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        RenderHelper.disableStandardItemLighting();

        // Calculate the color with alpha transparency
        int alpha = (int) (opacity * 255.0f);
        int color = (alpha << 24) | 0xFFFFFF; // Add the opacity to the color code

        // Draw the background rectangle
        //int textWidth = mc.fontRendererObj.getStringWidth(text);
        //int textHeight = mc.fontRendererObj.FONT_HEIGHT;
        //DrawingUtils.drawRect(-textWidth/2  - 3, 2, textWidth + 6, textHeight*2+5, new Color(47, 47, 47, 70));
        //mc.fontRendererObj.drawString(text, -textWidth / 2, 0, color);

        // Render the text
        int glyphWidth = glyphPageFontRenderer.getStringWidth(text);
        int glyphHeight = glyphPageFontRenderer.getFontHeight();

        // Define margin
        int margin = 4;

        // Draw the background rectangle with margin + text
        if(bg){
            DrawingUtils.drawRect(-glyphWidth / 2 - margin, -glyphHeight / 2 - margin/2, glyphWidth / 2 + margin, glyphHeight / 2 + margin/2, new Color(47, 47, 47, 70));
            glyphPageFontRenderer.drawString(text, -glyphWidth / 2, -glyphHeight / 2, color, true);
        }else{
            glyphPageFontRenderer.drawString(text, -glyphWidth / 2, -glyphHeight / 2, color, false);
        }


        //glyphPageFontRenderer.drawString(text, -glyphWidth/2, 0, -1, true);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
