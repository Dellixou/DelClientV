package com.github.dellixou.delclientv3.gui;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.macro.AutoForaging;
import com.github.dellixou.delclientv3.modules.macro.AutoPowder;
import com.github.dellixou.delclientv3.utils.ColorUtils;
import com.github.dellixou.delclientv3.utils.gui.shaders.Blur;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.shaders.misc.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * Handles in-game rendering hooks, including the module list and mouse interactions.
 */
public class GuiIngameHook {

    // Values
    public boolean enabled = false;
    private final boolean draggingModules = false;

    private static final GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 28, true, true, true);
    private static final GlyphPageFontRenderer helpFulFont = GlyphPageFontRenderer.create("Arial", 24, true, true, true);
    float scaleText = 0.6f;
    private float margin = 10;
    public long startTime;

    // Animations
    private static final float MIN_POWER = 0.3f;
    private static final float MAX_POWER = 0.5f;
    private static final long ANIMATION_DURATION = 5000;
    private float hudOpenProgress = 0f;
    private static final float ANIMATION_SPEED = 0.03f;


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

                // Render HUD Auto Foraging
                //AutoForaging autoForaging = (AutoForaging) ModuleManager.getModuleById("auto_fora");
                //renderAutoForagingHUD(mc, autoForaging);

            }
        }
    }

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
                    Gui.drawRect((int)(textX - 2), (int)textY, (int)(textX + moduleWidth * scaleText + 2), (int)(textY + rectHeight + 1), colorBG);
                    //Blur.getInstance().renderBlurSection((int)(textX - 2), (int)textY, (int)(textX + moduleWidth * scaleText + 2), (int)(rectHeight + 1), 10);
                }else if(index == toggledModules.size()-1){
                    Gui.drawRect((int)(textX - 2), (int)textY + 1, (int)(textX + moduleWidth * scaleText + 2), (int)(textY + rectHeight + 2), colorBG);
                    //Blur.getInstance().renderBlurSection((int)(textX - 2), (int)textY + 1, (int)(textX + moduleWidth * scaleText + 2), (int)(rectHeight + 1), 10);
                }else{
                    Gui.drawRect((int)(textX - 2), (int)textY + 1, (int)(textX + moduleWidth * scaleText + 2), (int)(textY + rectHeight + 1), colorBG);
                    //Blur.getInstance().renderBlurSection((int)(textX - 2), (int)textY + 0.8f, (int)(textX + moduleWidth * scaleText + 2), (int)(rectHeight), 10);
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
    }

    // ---------------------------------------------- AUTO FORAGING HUD ----------------------------------------------

    /**
     * Renders the HUD for Auto Powder.
     */
    private void renderAutoForagingHUD(Minecraft mc, AutoForaging autoForaging){
        // Somes values
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        float rectX = 0 + margin*1.3f; // screenWidth / 2 + margin * 2
        float rectY = screenHeight / 5f + margin;
        float rectWidth = 120;
        float rectHeight = 125;

        float textMargin = 2.0f;
        float scaledMargin = textMargin / scaleText;

        // Background
        long currentTime = System.currentTimeMillis();
        float progress = (float)((currentTime - startTime) % ANIMATION_DURATION) / ANIMATION_DURATION;
        float oscillation = (float) Math.sin(progress * 2 * Math.PI);
        float powerRange = MAX_POWER - MIN_POWER;
        float power = MIN_POWER + (oscillation + 1) * 0.5f * powerRange;

        // Background
        Color a = new Color(29, 29, 29, 180);
        Color b = new Color(
                (int)(ColorUtils.getClickGUIColor().getRed() * power),
                (int)(ColorUtils.getClickGUIColor().getGreen() * power),
                (int)(ColorUtils.getClickGUIColor().getBlue() * power),
                160
        );

        if (autoForaging.isToggled()) {
            hudOpenProgress = Math.min(1f, hudOpenProgress + ANIMATION_SPEED);
        } else {
            hudOpenProgress = Math.max(0f, hudOpenProgress - ANIMATION_SPEED);
        }

        if (hudOpenProgress == 0f) return;

        float animatedProgress = easeInOutQuart(hudOpenProgress);
        float animatedRectHeight = rectHeight * animatedProgress;

        Blur.getInstance().renderBlurSection(rectX, rectY, rectWidth, animatedRectHeight, 15);

        GlStateManager.pushMatrix();
        GlStateManager.translate(rectX, rectY + animatedRectHeight, 0);
        GlStateManager.scale(1, animatedProgress, 1);
        GlStateManager.translate(0, -rectHeight, 0);
        // Render text elements
        renderAutoForagingText(0, 0, scaledMargin, scaleText, autoForaging, rectWidth);

        GlStateManager.popMatrix();
    }

    private void renderAutoForagingText(float rectX, float rectY, float scaledMargin, float scaleText, AutoForaging autoForaging, float rectWidth) {
        float textY = 2;
        float lineHeight = helpFulFont.getFontHeight() * scaleText + 2; // Ajout d'un espacement supplémentaire

        renderTextLine("§lAuto Foraging : ", rectX-2, textY, scaledMargin, scaleText+0.1f);
        textY += lineHeight;

        DrawHelper.drawRoundedRect(rectX+2, textY + 2, rectWidth-4, 1, 1, new Color(59, 59, 59, 157));
        textY+= 4;

        renderTextLine("§a§lSkills", rectX-2, textY, scaledMargin, scaleText+0.1f);
        textY += lineHeight;

        renderTextLine("§fForaging Level: 55", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine("§fProgress: 54.8%", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine("§fXP/h: 50K/h", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine("§fProgress to max: 105%", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        DrawHelper.drawRoundedRect(rectX+2, textY + 2, rectWidth-4, 1, 1, new Color(59, 59, 59, 157));
        textY+= 4;

        renderTextLine("§a§lProfits", rectX-2, textY, scaledMargin, scaleText+0.1f);
        textY += lineHeight;

        renderTextLine("§fInventory Value: §c0$", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine("§fTotal Profit: §c0$", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine("§fPer Hour: §c0$/h", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine("§fStack Prize: §a64K$", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;
    }

    // ---------------------------------------------- AUTO POWDER HUD ----------------------------------------------

    /**
     * Renders the HUD for Auto Powder.
     */
    private void renderAutoPowderHUD(Minecraft mc, AutoPowder autoPowder){
        // Somes values
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        float rectX = 0 + margin*1.3f; // screenWidth / 2 + margin * 2
        float rectY = screenHeight / 3f + margin;
        float rectWidth = 130;
        float rectHeight = 75;

        float textMargin = 2.0f;
        float scaledMargin = textMargin / scaleText;

        // Background
        long currentTime = System.currentTimeMillis();
        float progress = (float)((currentTime - startTime) % ANIMATION_DURATION) / ANIMATION_DURATION;
        float oscillation = (float) Math.sin(progress * 2 * Math.PI);
        float powerRange = MAX_POWER - MIN_POWER;
        float power = MIN_POWER + (oscillation + 1) * 0.5f * powerRange;

        // Background
        Color a = new Color(29, 29, 29, 180);
        Color b = new Color(
                (int)(ColorUtils.getClickGUIColor().getRed() * power),
                (int)(ColorUtils.getClickGUIColor().getGreen() * power),
                (int)(ColorUtils.getClickGUIColor().getBlue() * power),
                160
        );

        if (autoPowder.isToggled()) {
            hudOpenProgress = Math.min(1f, hudOpenProgress + ANIMATION_SPEED);
        } else {
            hudOpenProgress = Math.max(0f, hudOpenProgress - ANIMATION_SPEED);
        }

        if (hudOpenProgress == 0f) return;

        float animatedProgress = easeInOutQuart(hudOpenProgress);
        float animatedRectHeight = rectHeight * animatedProgress;

        // Background with animation
        //Blur.getInstance().renderBlurSection(rectX+2, rectY+2, rectWidth-4, animatedRectHeight-4, 15);
        DrawHelper.drawRoundedGradientBlurredRect(rectX, rectY+animatedRectHeight, rectWidth, animatedRectHeight, 4, 7, a, a, b, b);

        DrawHelper.drawRoundedRectOutline(rectX, rectY+animatedRectHeight, rectWidth, animatedRectHeight, 4, 1.5f, ColorUtils.getClickGUIColor().darker());

        GlStateManager.pushMatrix();
        GlStateManager.translate(rectX, rectY + animatedRectHeight, 0);
        GlStateManager.scale(1, animatedProgress, 1);
        GlStateManager.translate(0, -rectHeight, 0);

        // Render text elements
        renderHUDText(0, 0, scaledMargin, scaleText, autoPowder);

        GlStateManager.popMatrix();
    }

    private void renderHUDText(float rectX, float rectY, float scaledMargin, float scaleText, AutoPowder autoPowder) {
        float textY = 2;
        float lineHeight = helpFulFont.getFontHeight() * scaleText + 2; // Ajout d'un espacement supplémentaire

        renderTextLine("§lAuto Powder V2 : ", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        String targetText = autoPowder.targetChest == null ? "§fTarget : §cNo target" : String.format("§fTarget : §aX: %d Y: %d Z: %d", autoPowder.targetChest.getX(), autoPowder.targetChest.getY(), autoPowder.targetChest.getZ());
        renderTextLine(targetText, rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine(autoPowder.isMining ? "§fIs mining : §aTrue" : "§fIs mining : §cFalse", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine(autoPowder.isReturning ? "§fIs returning : §aTrue" : "§fIs returning : §cFalse", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine(autoPowder.isLooking ? "§fIs looking : §aTrue" : "§fIs looking : §cFalse", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        renderTextLine(autoPowder.isClicking ? "§fIs clicking : §aTrue" : "§fIs clicking : §cFalse", rectX, textY, scaledMargin, scaleText);
        textY += lineHeight;

        String chestsText = autoPowder.detectedChests.size() > 0 ? "§fDetected chests : §a" + autoPowder.detectedChests.size() : "§fDetected chests : §c0";
        renderTextLine(chestsText, rectX, textY, scaledMargin, scaleText);
    }

    // ---------------------------------------------- MISC ----------------------------------------------

    private void renderTextLine(String text, float x, float y, float scaledMargin, float scaleText) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + scaledMargin, y, 0);
        GlStateManager.scale(scaleText, scaleText, scaleText);
        helpFulFont.drawString(text, 0, 0, -1, true);
        GlStateManager.popMatrix();
    }

    /**
     * Checks if the mouse is hovered over a specific area.
     */
    private boolean isHovered(int mouseX, int mouseY, int xStart, int xEnd, int yStart, int yEnd) {
        return mouseX >= xStart && mouseX <= xEnd && mouseY >= yStart && mouseY <= yEnd;
    }

    /**
     * Make rainbow.
     */
    private static int rainbow(int delay){
        float v1 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_1").getValDouble();
        float v2 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_2").getValDouble();

        double rainbowstate = Math.ceil((System.currentTimeMillis() + delay) / 20);
        rainbowstate %= 360;
        return Color.getHSBColor((float) (rainbowstate/360), v1/10, v2/10).getRGB();
    }

    /**
     * Ease In Out Quart
     */
    private static float easeInOutQuart(float x) {
        return x < 0.5 ? 8 * x * x * x * x : 1 - (float)Math.pow(-2 * x + 2, 4) / 2;
    }

}
