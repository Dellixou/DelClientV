package com.github.dellixou.delclientv3.events.ticks;

import com.github.dellixou.delclientv3.modules.floor7.AutoLeaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class TickAutoLeapsEvent {

    // Fields for managing AutoLeaps;
    public int tickCounter = 0;
    private static final int INTERVAL = 4; // 0.2 seconds (4 ticks)
    public AutoLeaps autoLeaps;

    /**
     * Every tick add a tick to counter and do search.
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        tickCounter++;
    }

    /**
     * Render text to know stage + tick
     */
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        drawTextBelowCrosshair("Stage: " + autoLeaps.currentStage, 0);
        drawTextBelowCrosshair("Tick: " + tickCounter, 5);
    }

    /**
     * Function to draw text
     */
    public static void drawTextBelowCrosshair(String text, int padding) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fontRenderer = mc.fontRendererObj;

        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int textWidth = fontRenderer.getStringWidth(text);

        // Set the scale factor for smaller text
        float scaleFactor = 0.6f;

        // Calculate the position to draw the text
        int x = (int) ((screenWidth - textWidth * scaleFactor) / 2);
        int y = (int) (screenHeight / 2 + 10 * scaleFactor + padding);

        // Apply scaling
        GL11.glPushMatrix();
        GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);

        // Adjust the position for the scaled text
        fontRenderer.drawStringWithShadow(text, x / scaleFactor, y / scaleFactor, 0xFFFFFF);

        // Restore scaling
        GL11.glPopMatrix();
    }


}
