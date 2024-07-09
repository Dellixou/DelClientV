package com.github.dellixou.delclientv3.gui;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.clickgui.util.ColorUtil;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.macro.AutoPowderV2;
import com.github.dellixou.delclientv3.utils.gui.Blur;
import com.github.dellixou.delclientv3.utils.gui.DrawingUtils;
import com.github.dellixou.delclientv3.utils.gui.Wrapper;
import com.github.dellixou.delclientv3.utils.gui.animations.FadeInAnimation;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.misc.BlurHelper;
import com.github.dellixou.delclientv3.utils.gui.misc.DrawHelper;
import com.github.dellixou.delclientv3.utils.gui.misc.ShaderHelper;
import net.minecraft.client.Minecraft;
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

    private final FadeInAnimation hoverAnim = new FadeInAnimation(60, 30);
    private static final GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 28, true, true, true);
    private static final GlyphPageFontRenderer helpFulFont = GlyphPageFontRenderer.create("Arial", 24, true, true, true);
    float scaleText = 0.6f;
    private float margin = 10;
    public long startTime;

    // Animations
    private static final float MIN_POWER = 0.3f;
    private static final float MAX_POWER = 0.5f;
    private static final long ANIMATION_DURATION = 5000;

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

                // Render HUD Auto Powder
                if(ModuleManager.getModuleById("auto_powderv2").isToggled()){
                    AutoPowderV2 autoPowder = (AutoPowderV2) ModuleManager.getModuleById("auto_powderv2");
                    renderAutoPowderHUD(mc, autoPowder);
                }
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
     * Renders the HUD for Auto Powder.
     */
    private void renderAutoPowderHUD(Minecraft mc, AutoPowderV2 autoPowder){
        // Somes values
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        float rectX = screenWidth / 2 + margin*2;
        float rectY = screenHeight / 2 + margin;
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
                (int)(ColorUtil.getClickGUIColor().getRed() * power),
                (int)(ColorUtil.getClickGUIColor().getGreen() * power),
                (int)(ColorUtil.getClickGUIColor().getBlue() * power),
                160
        );
        //DrawHelper.drawRoundedGradientBlurredRect(rectX, rectY+rectHeight, rectWidth, rectHeight, 4, 7, a, a, b, b);
        DrawHelper.drawRoundedRectOutline(rectX, rectY+rectHeight, rectWidth, rectHeight, 4, 1.5f, ColorUtil.getClickGUIColor().darker()); // new Color(29, 29, 29, 255)

        Blur.getInstance().renderBlurSection(rectX, rectY, rectWidth, rectHeight, 10);


        GlStateManager.pushMatrix();
        // Title text
        GlStateManager.translate(rectX + scaledMargin, rectY + scaledMargin, 0);
        GlStateManager.scale(scaleText, scaleText, scaleText);
        helpFulFont.drawString("§lAuto Powder V2 : ", 0, 0, -1, true);
        GlStateManager.popMatrix();
        // Is having a chest in target text
        GlStateManager.pushMatrix();
        float scale = (textMargin + 6)/scaleText;
        GlStateManager.translate(rectX + scaledMargin, rectY + scale, 0);
        GlStateManager.scale(scaleText, scaleText, scaleText);
        String text = autoPowder.targetChest == null ? "§fTarget : §cNo target" : "§fTarget : §aX: " + autoPowder.targetChest.getX() + " Y: " + autoPowder.targetChest.getY() + " Z: " + autoPowder.targetChest.getZ();
        helpFulFont.drawString(text, 0, 0, -1, true);
        GlStateManager.popMatrix();
        // Is mining text
        GlStateManager.pushMatrix();
        scale = (textMargin + 12)/scaleText;
        GlStateManager.translate(rectX + scaledMargin, rectY + scale, 0);
        GlStateManager.scale(scaleText, scaleText, scaleText);
        text = !autoPowder.isMining ? "§fIs mining : §cFalse" : "§fIs mining : §a True";
        helpFulFont.drawString(text, 0, 0, -1, true);
        GlStateManager.popMatrix();
        // Is returning text
        GlStateManager.pushMatrix();
        scale = (textMargin + 18)/scaleText;
        GlStateManager.translate(rectX + scaledMargin, rectY + scale, 0);
        GlStateManager.scale(scaleText, scaleText, scaleText);
        text = !autoPowder.isReturning ? "§fIs returning : §cFalse" : "§fIs returning : §a True";
        helpFulFont.drawString(text, 0, 0, -1, true);
        GlStateManager.popMatrix();
        // Is looking text
        GlStateManager.pushMatrix();
        scale = (textMargin + 24)/scaleText;
        GlStateManager.translate(rectX + scaledMargin, rectY + scale, 0);
        GlStateManager.scale(scaleText, scaleText, scaleText);
        text = !autoPowder.isLooking ? "§fIs looking : §cFalse" : "§fIs looking : §a True";
        helpFulFont.drawString(text, 0, 0, -1, true);
        GlStateManager.popMatrix();
        // Is clicking text
        GlStateManager.pushMatrix();
        scale = (textMargin + 30)/scaleText;
        GlStateManager.translate(rectX + scaledMargin, rectY + scale, 0);
        GlStateManager.scale(scaleText, scaleText, scaleText);
        text = !autoPowder.isClicking ? "§fIs clicking : §cFalse" : "§fIs clicking : §a True";
        helpFulFont.drawString(text, 0, 0, -1, true);
        GlStateManager.popMatrix();
        // Detected chests number
        GlStateManager.pushMatrix();
        scale = (textMargin + 36)/scaleText;
        GlStateManager.translate(rectX + scaledMargin, rectY + scale, 0);
        GlStateManager.scale(scaleText, scaleText, scaleText);
        text =  autoPowder.detectedChests.size() > 0 ? "§fDetected chests : §a" + autoPowder.detectedChests.size() : "§fDetected chests : §c0";
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

}
