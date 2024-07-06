package com.github.dellixou.delclientv3.gui;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.utils.Color.RainbowColor;
import com.github.dellixou.delclientv3.utils.gui.DrawingUtils;
import com.github.dellixou.delclientv3.utils.gui.Wrapper;
import com.github.dellixou.delclientv3.utils.gui.animations.FadeInAnimation;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPage;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

/**
 * Handles in-game rendering hooks, including the module list and mouse interactions.
 */
public class GuiIngameHook {

    // Values
    public boolean enabled = false;

    private final FadeInAnimation hoverAnim = new FadeInAnimation(60, 30);
    private static final GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 28, true, true, true);
    float scaleText = 0.6f;

    private final boolean draggingModules = false;


    /**
     * Renders the list of active modules and highlights them when the mouse hovers over.
     */
    private void renderModulesList() {
        int index = 0;
        long x = 0;

        // Get the current screen dimensions
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        // Calculate the starting position for rendering modules (top right corner)
        int widthLoc = screenWidth - 5; // 5 pixels from the right edge
        int heightLoc = 5; // 5 pixels from the top edge

        int yCount = heightLoc;
        int maxWidth = 0;
        int topY = yCount + 10;
        boolean hasActiveModules = false;

        final int[] counter = {1};

        List<Module> toggledModules = ModuleManager.getModules().stream()
                .filter(Module::isToggled)
                .collect(Collectors.toList());

        // Sort the modules by the width of their names in descending order
        Collections.sort(toggledModules, (m1, m2) -> {
            int width1 = glyphPageFontRenderer.getStringWidth("- " + m1.getName());
            int width2 = glyphPageFontRenderer.getStringWidth("- " + m2.getName());
            return width2 - width1;
        });

        for (Module m : toggledModules) {
            m.onRender();

            if (m.isToggled()) {
                hasActiveModules = true;

                // Get width text
                int moduleWidth = glyphPageFontRenderer.getStringWidth("- " + m.getName());

                GlStateManager.pushMatrix();

                // Adjust the x position to align the text to the right
                float textX = widthLoc - moduleWidth * scaleText;
                float textY = yCount;

                // Calculate the height of the rectangle background
                float rectHeight = glyphPageFontRenderer.getFontHeight() * scaleText - 1;

                // Background Module
                int colorBG = DelClient.settingsManager.getSettingById("module_list_bg_rainbow").getValBoolean() ? rainbow(counter[0] * 250) : new Color(0, 0, 0, 127).getRGB();
                if(index == 0){
                    DrawingUtils.drawRect(textX - 2, textY, textX + moduleWidth * scaleText + 2, textY + rectHeight + 1, colorBG);
                }else if(index == toggledModules.size()-1){
                    DrawingUtils.drawRect(textX - 2, textY + 1, textX + moduleWidth * scaleText + 2, textY + rectHeight + 2, colorBG);
                }else{
                    DrawingUtils.drawRect(textX - 2, textY + 1, textX + moduleWidth * scaleText + 2, textY + rectHeight + 1, colorBG);
                }

                // Text Module
                GlStateManager.translate(textX, textY, 0);
                GlStateManager.scale(scaleText, scaleText, scaleText);
                int colorText = DelClient.settingsManager.getSettingById("module_list_text_rainbow").getValBoolean() ? rainbow(counter[0] * 250) : Color.WHITE.getRGB();
                glyphPageFontRenderer.drawString("- " + m.getName(), 0, 0, colorText, true);
                GlStateManager.popMatrix();

                // Some calculi
                if (moduleWidth > maxWidth) {
                    maxWidth = moduleWidth;
                }

                yCount += rectHeight; // Adjust spacing between modules
                index++;
                x++;
                counter[0]++;
            }
        }

        // Final adjustment for the bottom Y to include the last module's height
        if (hasActiveModules) {
            if (Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen()) {
                int x1 = (int) x;
                // Check if the mouse is within the bounds of the rectangle encompassing all modules
                if (isHovered((int) DelClient.instance.mouseXPos, (int) DelClient.instance.mouseYPos, widthLoc - 2, widthLoc + maxWidth + 3, topY - (int) (Wrapper.fr.FONT_HEIGHT * x), topY + 3)) {
                    DrawingUtils.drawRect(widthLoc - 2, topY - (int) (Wrapper.fr.FONT_HEIGHT * x) + 1, widthLoc + maxWidth + 3, topY + 3, new Color(255, 255, 255, hoverAnim.getProgress()));
                } else {
                    hoverAnim.reset();
                }
            }
        }
    }

    /**
     * Handles the pre-render event for the game overlay.
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.gameSettings.showDebugInfo) {
            if (event.type == RenderGameOverlayEvent.ElementType.EXPERIENCE) {
                // Render Module
                if(enabled){renderModulesList();}


                if(!DelClient.instance.getIsAuthorized()){
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(5, 5, 0);
                    GlStateManager.scale(0.7, 0.7, 0.7);
                    glyphPageFontRenderer.drawString("§fDel the goat fr fr stfu del the goat!", 0, 0, -1, true);
                    GlStateManager.popMatrix();
                }
            }
        }
    }


    /**
     * Checks if the mouse is hovered over a specific area.
     */
    private boolean isHovered(int mouseX, int mouseY, int xStart, int xEnd, int yStart, int yEnd) {
        return mouseX >= xStart && mouseX <= xEnd && mouseY >= yStart && mouseY <= yEnd;
    }

    private static int rainbow(int delay){
        float v1 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_1").getValDouble();
        float v2 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_2").getValDouble();

        double rainbowstate = Math.ceil((System.currentTimeMillis() + delay) / 20);
        rainbowstate %= 360;
        return Color.getHSBColor((float) (rainbowstate/360), v1/10, v2/10).getRGB();
    }

}
