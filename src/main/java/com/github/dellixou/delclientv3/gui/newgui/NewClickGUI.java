package com.github.dellixou.delclientv3.gui.newgui;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.clickgui.util.ColorUtil;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.utils.Color.AnimatedColor;
import com.github.dellixou.delclientv3.utils.gui.DrawingUtils;
import com.github.dellixou.delclientv3.utils.gui.animations.LinearAnimation;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.misc.DrawHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.GLU;

import static org.lwjgl.opengl.GL11.glEnable;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewClickGUI extends GuiScreen {

    // Animations
    public LinearAnimation popAnim;
    public LinearAnimation searchBoxFocus;
    public LinearAnimation settingsPanelAnim;

    // Settings Animations

    // Colors
    private final AnimatedColor checkBoxColor = new AnimatedColor(new Color(64, 64, 64, 255), ColorUtil.getClickGUIColor(), 200, true);

    // Fonts
    private static final GlyphPageFontRenderer font = GlyphPageFontRenderer.create("Arial", 24, true, true, true);

    // Background
    public int BGWidth = 320;
    public int BGHeight = 210;
    public int rectX;
    public int rectY;

    // Buttons
    private int buttonH = 30;
    private int buttonP = 10;

    // Scroll variables
    private int maxScroll;
    private float scrollAmount = 0f;
    private float targetScrollAmount = 0f;
    private float currentScrollAmount = 0f;
    private float lerpSpeedScroll = 0.2f;

    // Scroll bar
    private int scrollBarWidth = 2;
    private int scrollBarHeight;
    private int scrollBarX;
    private int scrollBarY;
    private int scrollBarThumbHeight;
    private int scrollBarThumbY;
    private boolean isDraggingScrollBar = false;

    // Search box
    private String searchText = "";
    private String lastSearchText = "";
    private boolean isSearchBoxFocused = false;
    private int searchBoxX;
    private int searchBoxY;
    private int searchBoxWidth = 70;
    private int searchBoxHeight = 15;
    private boolean isSearchBoxAnimating = false;
    private boolean isSearchBoxFinishedAnim = false;

    // Modules
    private List<NewModuleButton> buttons = new ArrayList<>();
    public List<NewModuleButton> blackListMod = new ArrayList<>();

    // Settings
    private boolean isBackSettingHover = false;
    public boolean isSettingOpen = false;
    private Color settingBGColor = new Color(47, 47, 56, 255);
    private Setting currentSettingHover;

    // Settings Scroll
    public float settingsScrollAmount = 0f;
    private float settingsTargetScrollAmount = 0f;
    public float settingsCurrentScrollAmount = 0f;
    private float settingsMaxScroll = 0f;
    private float settingsLerpSpeedScroll = 0.2f;

    // Categories
    public Map<Category, ResourceLocation> categoryIcon;
    public Category selectedCategory = Category.RENDER;
    public Category currentCategoryHover;
    public Category lastCategory = Category.RENDER;

    /*
     * Init.
     */
    @Override
    public void initGui() {
        // Background
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        rectX = (screenWidth - BGWidth) / 2;
        rectY = (screenHeight - BGHeight) / 2;

        // Animations
        popAnim = new LinearAnimation(0, 1, 200);
        popAnim.setIsDrawAnimation(true);

        // Initialize modules
        int rectWidth = 160;
        int rectHeight = 30;
        int padding = 10;

        buttons.clear();
        int i = 0;
        for(Module mod : ModuleManager.getModules()){
            NewModuleButton newModuleButton = new NewModuleButton(mod, null, 10, (rectHeight + padding) * i, rectWidth, rectHeight);
            buttons.add(newModuleButton);
            buttons.get(i).clickGUI = this;
            buttons.get(i).index = i;
            buttons.get(i).updateCheckBoxState();
            if(mod.getId() == "click_gui"){
                blackListMod.add(newModuleButton);
            }
            buttons.get(i).checkBlacklist();
            buttons.get(i).updateSettings();
            i++;
        }

        categoryIcon = new HashMap<>();
        for(Category cat : Category.values()){
            if(cat.equals(Category.FLOOR7)){
                categoryIcon.put(cat, new ResourceLocation("textures/dungeon.png"));
            }else if(cat.equals(Category.MOVEMENT)){
                categoryIcon.put(cat, new ResourceLocation("textures/movement.png"));
            }else if(cat.equals(Category.RENDER)){
                categoryIcon.put(cat, new ResourceLocation("textures/render.png"));
            }else if(cat.equals(Category.MACRO)){
                categoryIcon.put(cat, new ResourceLocation("textures/macro.png"));
            }else if(cat.equals(Category.PLAYER)){
                categoryIcon.put(cat, new ResourceLocation("textures/player.png"));
            }else if(cat.equals(Category.MISC)){
                categoryIcon.put(cat, new ResourceLocation("textures/misc.png"));
            }
        }

        // Scroll
        maxScroll = Math.max(0, buttons.size() * (22 + 5) - (rectHeight - 20));

        // Scroll bar
        scrollBarHeight = BGHeight - 20;
        scrollBarX = rectX + BGWidth - scrollBarWidth - 5;
        scrollBarY = rectY + 10;
        updateScrollBarThumb();

        // Search box
        searchBoxX = rectX + 10;
        searchBoxY = rectY + 10;

        // Settings
        settingsPanelAnim = new LinearAnimation(0, 1, 300);
        settingsPanelAnim.setIsDrawAnimation(true);

        isSettingOpen = false;
        isBackSettingHover = false;

    }

    @Override
    public void onGuiClosed() {
        // End Blur
        if(mc.entityRenderer.isShaderActive()){
            mc.entityRenderer.stopUseShader();
        }

        // Save Mods
        DelClient.fileManager.saveMods();
    }

    /*
     * No pause for single player.
     */
    public boolean doesGuiPauseGame(){
        return false;
    }

    /*
     * Draw Screen.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Animations
        if(searchBoxFocus != null){
            if(isSearchBoxFocused){
                float temp = searchBoxFocus.getAnimationValue()/100;
                searchBoxWidth = (int)(easeOutBack(temp) * 100);
            }else{
                searchBoxWidth = (int) searchBoxFocus.getAnimationValue();
            }
        }

        // Some values
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int buttonXPos = 175;
        int sideMargin = 10;

        // -------------------------------------------- BACKGROUND --------------------------------------------

        float scale = popAnim.getAnimationValue();
        scale = easeOutCirc(scale);
        GlStateManager.pushMatrix();
        GlStateManager.translate(screenWidth / 2, screenHeight / 2, 0);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-screenWidth / 2, -screenHeight / 2, 0);

        // Draw the rectangle
        DrawHelper.drawRoundedBlurredRect(rectX-7.5f, rectY+BGHeight-7.5f+15, BGWidth+15, BGHeight+15, 3, 11, new Color(21, 21, 21, 150));
        DrawHelper.drawRoundedRect(rectX, rectY+BGHeight, BGWidth, BGHeight, 3, new Color(21, 21, 26, 255));


        // Little line between two parts
        DrawHelper.drawRoundedRect(rectX+BGWidth-buttonXPos-sideMargin , rectY+10+(BGHeight-20), 2, BGHeight-20, 1, new Color(36, 36, 36, 123));


        // -------------------------------------------- RIGHT SIDE --------------------------------------------

        currentScrollAmount = lerp(currentScrollAmount, targetScrollAmount, lerpSpeedScroll * partialTicks);
        scrollAmount = currentScrollAmount;

        // Draw Modules Buttons
        glEnable(GL11.GL_SCISSOR_TEST);
        scissor(rectX + BGWidth - buttonXPos - sideMargin * 2, rectY + 10, buttonXPos + sideMargin, BGHeight - 20);

        GlStateManager.pushMatrix();
        GlStateManager.translate(rectX + BGWidth - buttonXPos - sideMargin, rectY + 10 - (int)scrollAmount, 0);

        int translatedMouseX = mouseX - (rectX + BGWidth - buttonXPos - sideMargin);
        int translatedMouseY = mouseY - (rectY + 10 - (int)scrollAmount);

        boolean hoverGUI;
        if(isMouseOverGUI(mouseX, mouseY)){
            hoverGUI = true;
        }else{
            hoverGUI = false;
        }

        if (!searchText.equals(lastSearchText)) {
            targetScrollAmount = 0;
            currentScrollAmount = 0;
            scrollAmount = 0;
            lastSearchText = searchText;
        }
        if(!selectedCategory.equals(lastCategory)){
            targetScrollAmount = 0;
            currentScrollAmount = 0;
            scrollAmount = 0;
            lastCategory = selectedCategory;
        }

        int visibleIndex = 0;
        for (NewModuleButton button : buttons) {
            if (searchText.isEmpty() || button.mod.getName().toLowerCase().contains(searchText.toLowerCase())) {
                button.y = (buttonH + buttonP) * visibleIndex;
                visibleIndex++;
            }
        }

        maxScroll = Math.max(0, visibleIndex * (buttonH + buttonP) - (BGHeight - 20));

        for (NewModuleButton button : buttons) {
            if(searchText.isEmpty()){
                if(button.mod.getCategory().equals(selectedCategory)){
                    button.drawScreen(translatedMouseX, translatedMouseY, partialTicks, button.x, button.y, button.width, button.height);
                    button.canHover = hoverGUI && !isSettingOpen;
                }
            }else if(button.mod.getName().toLowerCase().contains(searchText.toLowerCase())){
                button.drawScreen(translatedMouseX, translatedMouseY, partialTicks, button.x, button.y, button.width, button.height);
                button.canHover = hoverGUI && !isSettingOpen;
            }
        }

        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Slide bar
        drawScrollBar(mouseX, mouseY, partialTicks);

        // -------------------------------------------- LEFT SIDE ----------------------------------------------

        // Search box
        drawSearchBox(mouseX, mouseY, partialTicks, sr);

        // Categories
        float categoryHeight = 0;
        for(Category category : Category.values()){
            drawCategoryButton(category, rectX + 10, rectY + 35 + categoryHeight, 100, 20, mouseX, mouseY, partialTicks);
            categoryHeight+=28;
        }

        // -------------------------------------------- SETTINGS --------------------------------------------

        // TODO: Combo box pour choisir les animations : ease ..

        for(NewModuleButton button : buttons){
            if(button.isSettingOpen || button.isClosingSettings){
                button.isSettingOpen = true;
                // Settings Panel
                float Sx = rectX + BGWidth - buttonXPos - sideMargin * 2;
                float Sy = rectY;
                float Swidth = buttonXPos + sideMargin * 2;
                float Sheight = BGHeight;

                float scaleX = button.isClosingSettings ? 1 - settingsPanelAnim.getAnimationValue() : settingsPanelAnim.getAnimationValue();
                scaleX = easeOutBack(scaleX);

                // Background
                GlStateManager.pushMatrix();
                GlStateManager.translate(Sx + Swidth, Sy, 0);
                GlStateManager.scale(scaleX, 1, 1);
                GlStateManager.translate(-(Sx + Swidth), -Sy, 0);
                DrawHelper.drawRoundedRect(Sx, Sy + Sheight, Swidth, Sheight, 3, new Color(39, 39, 49, 255));

                // Title Mod
                GlStateManager.pushMatrix();
                GlStateManager.translate(Sx + 25, Sy + font.getFontHeight()/2f*0.75f+3f, 0);
                GlStateManager.scale(0.75, 0.75, 0.75);
                font.drawString("§l" + button.mod.getName(), 0, 0, new Color(255, 255, 255, 255).getRGB(), true);
                GlStateManager.popMatrix();

                // Back button
                isBackSettingHover = isBackSettingOver(mouseX, mouseY, Sx, Sy, 32, 32);
                Color c = isBackSettingHover ? new Color(215, 215, 215, 176) : new Color(167, 167, 167, 176);

                GlStateManager.pushMatrix();
                GlStateManager.translate(Sx+3, Sy+3, 0);
                GlStateManager.scale(0.7, 0.7, 0.7);
                GlStateManager.translate(-(Sx+3), -(Sy+3), 0);
                DrawHelper.drawRoundedTexture(new ResourceLocation("textures/back.png"), Sx+3, Sy+3+32, 32, 32, 1, c);
                GlStateManager.popMatrix();

                if(button.mod.hasSettings()){
                    // Calculate the height for settings
                    float totalSettingsHeight = calculateTotalSettingsHeight(button);
                    settingsMaxScroll = Math.max(0, totalSettingsHeight - (Sheight - 40)); // 40 pour laisser de l'espace en haut et en bas

                    // Using lerp for mouse wheel
                    settingsCurrentScrollAmount = lerp(settingsCurrentScrollAmount, settingsTargetScrollAmount, settingsLerpSpeedScroll * partialTicks);
                    settingsScrollAmount = settingsCurrentScrollAmount;

                    // Apply scissor
                    glEnable(GL11.GL_SCISSOR_TEST);
                    scissorSettings(Sx-50, Sy + 25, Swidth+50, Sheight - 30);

                    // Draw settings
                    drawSettings(button, Sx, Sy + 25 - settingsScrollAmount, Swidth-6, mouseX, mouseY, partialTicks);

                    // Disable scissor
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
                }

                // Check if closing animation is finished
                if (button.isClosingSettings && settingsPanelAnim.isAnimationDone()) {
                    for(NewModuleButton button1 : buttons){
                        button1.isSettingOpen = false;
                        isSettingOpen = false;
                    }
                    button.isClosingSettings = false;
                }
                GlStateManager.popMatrix();
                break;
            }
        }

        // ------------------------------------------------------------------------------------------------------
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /*
     * Draw scroll bar.
     */
    private void drawScrollBar(int mouseX, int mouseY, float partialTicks) {
        // Slide background
        DrawHelper.drawRoundedRect(scrollBarX, scrollBarY+scrollBarHeight, scrollBarWidth, scrollBarHeight, 1, new Color(66, 66, 66, 200));
        // Slide
        DrawHelper.drawRoundedRect(scrollBarX, scrollBarThumbY+scrollBarThumbHeight, scrollBarWidth, scrollBarThumbHeight, 1, new Color(80, 80, 80, 255));

        // Dragging code
        if (isDraggingScrollBar) {
            int mouseYRelative = mouseY - scrollBarY;
            float percentage = (float) mouseYRelative / scrollBarHeight;
            targetScrollAmount = percentage * maxScroll;
            targetScrollAmount = MathHelper.clamp_float(targetScrollAmount, 0, maxScroll);
        }

        updateScrollBarThumb();
    }

    /*
     * Update scroll bar.
     */
    private void updateScrollBarThumb() {
        float contentHeight = buttons.size() * (buttonH + buttonP);
        float visibleRatio = Math.min(1, (BGHeight - 20) / contentHeight);
        scrollBarThumbHeight = Math.max(20, (int) (scrollBarHeight * visibleRatio));
        float scrollPercentage = currentScrollAmount / maxScroll;
        scrollBarThumbY = scrollBarY + (int) ((scrollBarHeight - scrollBarThumbHeight) * scrollPercentage);
    }

    /*
     * Settings calculate height.
     */
    private float calculateTotalSettingsHeight(NewModuleButton button) {
        float totalHeight = 0;
        for (String category : DelClient.settingsManager.getAllSettingsCategory(button.mod)) {
            List<Setting> categorySettings = DelClient.settingsManager.getSettingsByModAndCategory(button.mod, category);
            for (Setting setting : categorySettings) {
                if (setting.isCheck()) {
                    totalHeight += 17;
                }else if(setting.isSlider()){
                    totalHeight += 23;
                }else if(setting.isText()){
                    totalHeight += 23;
                }else if(setting.isCombo()){
                    totalHeight += 23;
                }
            }
            totalHeight += 5; // Espace entre les catégories
        }
        return totalHeight;
    }

    /*
     * Draw search box.
     */
    private void drawSearchBox(int mouseX, int mouseY, float partialTicks, ScaledResolution sr){

        if (!isSearchBoxFocused && searchBoxWidth > 70) {
            searchBoxFocus = new LinearAnimation(searchBoxWidth, 70, 100, true);
            isSearchBoxAnimating = true;
        }

        // Search box background
        DrawHelper.drawRoundedRect(searchBoxX, searchBoxY+searchBoxHeight, searchBoxWidth, searchBoxHeight, 6, new Color(40, 40, 40, 200));

        // Search icon
        GL11.glEnable(GL13.GL_MULTISAMPLE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(searchBoxX+3, searchBoxY+12.5f, 0);
        GlStateManager.scale(0.3, 0.3, 0.3);
        GlStateManager.translate(-(searchBoxX+3), -(searchBoxY+12.5f), 0);
        DrawHelper.drawRoundedTexture(new ResourceLocation("textures/search.png"), searchBoxX+3, searchBoxY+12.5f, 32, 32, 1, new Color(167, 167, 167, 176));
        GlStateManager.popMatrix();
        GL11.glDisable(GL13.GL_MULTISAMPLE);


        // Texte dans la search box
        if (searchText.isEmpty() && !isSearchBoxFocused) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(searchBoxX + 5 + 10, searchBoxY + 3.5f, 0);
            GlStateManager.scale(0.6, 0.6, 0.6);
            font.drawString("Search...", 0, 0, new Color(150, 150, 150, 200).getRGB(), false);
            GlStateManager.popMatrix();
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.translate(searchBoxX + 5 + 10, searchBoxY + 3.5f, 0);
            GlStateManager.scale(0.6, 0.6, 0.6);
            font.drawString(searchText, 0, 0, new Color(200, 200, 200, 200).getRGB(), false);
            GlStateManager.popMatrix();
        }

        // Cursor
        if (isSearchBoxFocused && System.currentTimeMillis() % 1000 > 500) {
            DrawHelper.drawRoundedRect(searchBoxX + 5 + 10 + font.getStringWidth(searchText)*0.6f + 1, searchBoxY + 3 + searchBoxHeight - 10 + 4, 1, searchBoxHeight - 10 + 4, 1, Color.WHITE);
        }
    }

    /*
     * Draw settings.
     */
    private void drawSettings(NewModuleButton button, float x, float y, float width, int mouseX, int mouseY, float partialTicks) {
        int offset = 0;
        for (String category : DelClient.settingsManager.getAllSettingsCategory(button.mod)) {
            List<Setting> categorySettings = DelClient.settingsManager.getSettingsByModAndCategory(button.mod, category);

            int categoryHeight = 0;
            for (Setting cat : categorySettings){
                if(cat.isCheck()) categoryHeight += 17;
                if(cat.isSlider()) categoryHeight += 23;
            }

            // Draw the backgrounds for each categories
            DrawHelper.drawRoundedRect(x + 3, y + offset + categoryHeight, width - 6, categoryHeight, 2, settingBGColor);

            // Draw parameters
            for (Setting setting : categorySettings) {
                if (setting.isCheck()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 6, y + offset + font.getFontHeight()/2*0.6, 0);
                    GlStateManager.scale(0.6, 0.6, 0.6);
                    font.drawString(setting.getName(), 0, 0, new Color(227, 227, 227, 200).getRGB(), false);
                    GlStateManager.popMatrix();

                    Color checkColor;
                    if (setting.getValBoolean()) {
                        checkColor = ColorUtil.getClickGUIColor();
                    } else {
                        checkColor = new Color(64, 64, 64, 255);
                    }
                    DrawHelper.drawRoundedRect(x + width - 27, y + offset + 4.5f + 9f, 18, 9, 4, checkColor);

                    if(setting.clickAnimCircle != null){
                        setting.xCircle = setting.clickAnimCircle.getAnimationValue();
                    }

                    float xx = x + width - setting.xCircle;

                    Color c = isHoveringSetting(mouseX, mouseY, x+width-27, y+offset+4.5f, 18, 9, setting) ? new Color(173, 173, 180) : Color.WHITE;
                    DrawHelper.drawCircle(xx, y+offset+9, 3, c);

                    // OFFSET
                    offset += 17;
                }

                if (setting.isSlider()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 6, y + offset + font.getFontHeight()/2*0.6, 0);
                    GlStateManager.scale(0.6, 0.6, 0.6);
                    font.drawString(setting.getName(), 0, 0, new Color(227, 227, 227, 200).getRGB(), false);
                    GlStateManager.popMatrix();

                    int padding = 8;
                    float displayValX = (float)(x + width - font.getStringWidth(String.valueOf(setting.getValDouble())) * (0.55) - padding);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(displayValX, y + offset + font.getFontHeight()/2*0.6, 0);
                    GlStateManager.scale(0.55, 0.55, 0.55);
                    font.drawString(String.valueOf(setting.getValDouble()), 0, 0, new Color(227, 227, 227, 200).getRGB(), false);
                    GlStateManager.popMatrix();

                    // Set value for slider
                    if (setting.dragging) {
                        double diff = setting.getMax() - setting.getMin();
                        double val = setting.getMin() + (MathHelper.clamp_double((mouseX - x) / width, 0, 1)) * diff;
                        setting.setValDouble(val); // Define value
                        if (button.mod.applyOnChange) {
                            if (button.mod.isToggled()) {
                                button.mod.onEnable();
                            }
                        }
                    }

                    // Target bar percent
                    setting.targetPercentBar = (float) ((setting.getValDouble() - setting.getMin()) / (setting.getMax() - setting.getMin()));
                    // Lerp for smooth
                    setting.currentPercentBar = lerp(setting.currentPercentBar, setting.targetPercentBar, setting.lerpSpeed * partialTicks);

                    // Background
                    DrawHelper.drawRoundedRect(x + 6, y + offset + 16f, width-12, 2, 1, new Color(0x232323));
                    // Percent
                    DrawHelper.drawRoundedRect(x + 6, y + offset + 16f, setting.currentPercentBar * (width - 12), 2, 1, ColorUtil.getClickGUIColor());

                    // Circle
                    Color c = isHoveringSetting(mouseX, mouseY, x+6, y+offset+12f, width-12, 5, setting) ? ColorUtil.getClickGUIColor().darker() : ColorUtil.getClickGUIColor();
                    if (setting.getValDouble() > setting.getMin() && setting.getValDouble() < setting.getMax()) {
                        DrawHelper.drawCircle(x + 6 + setting.currentPercentBar * (width - 12), y+offset+15, 2, c);
                    }

                    // OFFSET
                    offset += 23;
                }
            }
            offset += 5; // Space between parameters
        }
    }

    /*
     * Draw category button
     */
    private void drawCategoryButton(Category category, float x, float y, float width, float height, int mouseX, int mouseY, float partialTicks){

        if(selectedCategory.equals(category)){
            DrawHelper.drawRoundedRect(x, y + height, width, height, 3, new Color(28, 30, 31, 255));
        }

        float iconSize = 9.6f;
        int iconX = (int) (x + 5);
        int iconY = (int) (y + (height - iconSize) / 2);

        ResourceLocation iconCat = categoryIcon.get(category);

        GlStateManager.pushMatrix();
        GlStateManager.translate(iconX, iconY, 0);
        GlStateManager.scale(0.15, 0.15, 0.15);
        GlStateManager.translate(-iconX, -iconY, 0);
        DrawHelper.drawRoundedTexture(iconCat, iconX, iconY+64, 64, 64, 1, new Color(168, 168, 168, 255));
        GlStateManager.popMatrix();

        String text = "§l" + category.name().substring(0, 1).toUpperCase() + category.name().substring(1).toLowerCase();

        Color c = new Color(168, 168, 168, 255);
        if(isHoveringCategory(mouseX, mouseY, x, y, width, height, category)){
            c = Color.WHITE;
        }

        float textYZ = (y + (height - font.getFontHeight() * 0.6f) / 2);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 20, textYZ, 0);
        GlStateManager.scale(0.6f, 0.6f, 0.6f);
        font.drawString(text, 0, 0, c.getRGB(), true);
        GlStateManager.popMatrix();
    }

    /*
     * Wheel movement.
     */
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            if (isSettingOpen) {
                float scrollSpeed = 14f;
                settingsTargetScrollAmount += scroll > 0 ? -scrollSpeed : scrollSpeed;
                settingsTargetScrollAmount = MathHelper.clamp_float(settingsTargetScrollAmount, 0, settingsMaxScroll);
            } else {
                float scrollSpeed = 14f;
                targetScrollAmount += scroll > 0 ? -scrollSpeed : scrollSpeed;
                targetScrollAmount = MathHelper.clamp_float(targetScrollAmount, 0, maxScroll);
                updateScrollBarThumb();
            }
        }
    }

    // TODO : bouger les modules quand changement de catégorie
    /*
     * Mouse clicked.
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(currentCategoryHover != null){
            selectedCategory = currentCategoryHover;
        }
        if(currentSettingHover != null){
            isSearchBoxFocused = false;
            if(currentSettingHover.isCheck()){
                currentSettingHover.setClicked(!currentSettingHover.isClicked());
            }
            if(currentSettingHover.isSlider()){
                currentSettingHover.dragging = true;
            }
        }

        // CLICK SCROLL BAR
        if (mouseButton == 0 && isMouseOverScrollBar(mouseX, mouseY)) {
            isDraggingScrollBar = true;
            isSearchBoxFocused = false;
            isSearchBoxAnimating = false;
            searchBoxFocus = new LinearAnimation(searchBoxWidth, 70, 100, true);

        // CLICK SEARCH BOX
        }
        if(mouseButton == 0 && isMouseOverSearchBox(mouseX, mouseY)){
            if(!isSearchBoxFocused){
                isSearchBoxFocused = true;
                if(!isSearchBoxAnimating || !isSearchBoxFinishedAnim){
                    isSearchBoxAnimating = true;
                    searchBoxFocus = new LinearAnimation(searchBoxWidth, searchBoxWidth+40, 100, true);
                }
            }
        // CLICK BACK SETTINGS
        }
        else if(mouseButton == 0 && isBackSettingHover){
            for(NewModuleButton button : buttons){
                if(button.isSettingOpen){
                    button.isClosingSettings = true;
                }
            }
            // Reset search box state
            isSearchBoxFocused = false;
            isSearchBoxAnimating = false;
            searchBoxWidth = 70; // Reset to initial width
            searchBoxFocus = new LinearAnimation(searchBoxWidth, 70, 100, true);
            settingsPanelAnim.reset();
        }
        // CLICK NOTHING
        else{
            isSearchBoxFocused = false;
            if(isSearchBoxAnimating){
                searchBoxFocus = new LinearAnimation(searchBoxWidth, 70, 100, true);
                isSearchBoxAnimating = false;
            }
        }

        for (NewModuleButton button : buttons) {
            button.mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /*
     * Mouse released.
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for(Setting set : DelClient.settingsManager.getSettings()){
            if(set.isSlider()) set.dragging = false;
        }
        if (isDraggingScrollBar) {
            isDraggingScrollBar = false;
        } else {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    /*
     * Key typed.
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isSearchBoxFocused) {
            if (keyCode == Keyboard.KEY_BACK && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
            } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                searchText += typedChar;
            }
            // La recherche a changé, donc on réinitialise lastSearchText
            // pour déclencher le scroll au début au prochain drawScreen
            lastSearchText = "";
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    /*
     * Scissor element if not in UI range.
     */
    private void scissor(int x, int y, int width, int height) {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int scaleFactor = sr.getScaleFactor();

        float scale = popAnim.getAnimationValue();
        //scale = easeOutBack(scale);
        scale = easeOutCirc(scale);

        // Calculer les coordonnées et dimensions ajustées
        float adjustedX = (x - screenWidth / 2) * scale + screenWidth / 2;
        float adjustedY = (y - screenHeight / 2) * scale + screenHeight / 2;
        float adjustedWidth = width * scale;
        float adjustedHeight = height * scale;

        GL11.glScissor(
                (int)(adjustedX * scaleFactor),
                (int)(mc.displayHeight - (adjustedY + adjustedHeight) * scaleFactor),
                (int)(adjustedWidth * scaleFactor),
                (int)(adjustedHeight * scaleFactor)
        );
    }

    /*
     * Scissor element if not in UI range but for settings.
     */
    private void scissorSettings(float x, float y, float width, float height) {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int scaleFactor = sr.getScaleFactor();

        float scale = popAnim.getAnimationValue();
        scale = easeOutCirc(scale);

        float adjustedX = (x - screenWidth / 2) * scale + screenWidth / 2;
        float adjustedY = (y - screenHeight / 2) * scale + screenHeight / 2;
        float adjustedWidth = width * scale;
        float adjustedHeight = height * scale;

        GL11.glScissor(
                (int)(adjustedX * scaleFactor),
                (int)(mc.displayHeight - (adjustedY + adjustedHeight) * scaleFactor),
                (int)(adjustedWidth * scaleFactor),
                (int)(adjustedHeight * scaleFactor)
        );
    }


    // -------------------------------------------- HOVER --------------------------------------------

    private boolean isMouseOverScrollBar(int mouseX, int mouseY) {
        return mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarWidth &&
                mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight && !isSettingOpen;
    }

    private boolean isMouseOverSearchBox(int mouseX, int mouseY) {
        return mouseX >= searchBoxX && mouseX <= searchBoxX + searchBoxWidth &&
                mouseY >= searchBoxY && mouseY <= searchBoxY + searchBoxHeight;
    }

    private boolean isMouseOverGUI(int mouseX, int mouseY) {
        return mouseX >= rectX && mouseX <= rectX + BGWidth &&
                mouseY >= rectY + 10 && mouseY <= rectY + BGHeight - 20;
    }

    private boolean isBackSettingOver(int mouseX, int mouseY, float x, float y, float width, float height){
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    private boolean isHoveringSetting(int mouseX, int mouseY, float x, float y, float width, float height, Setting set){
        boolean hover =  mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
        if(hover){
            currentSettingHover = set;
        }else if(currentSettingHover == set){
            currentSettingHover = null;
        }
        return hover;
    }

    private boolean isHoveringCategory(int mouseX, int mouseY, float x, float y, float width, float height, Category category){
        boolean hover =  mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
        if(hover){
            currentCategoryHover = category;
        }else if(currentCategoryHover == category){
            currentSettingHover = null;
        }
        return hover;
    }

    // -------------------------------------------- Easing --------------------------------------------

    private float easeOutBack(float x){
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
    }

    private float easeOutQuart(float x){
        return (float) (1 - Math.pow(1 - x, 4));
    }

    private float easeOutCirc(float x){
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    private float lerp(float start, float end, float amount) {
        return start + amount * (end - start);
    }

}
