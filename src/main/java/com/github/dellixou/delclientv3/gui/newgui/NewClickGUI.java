package com.github.dellixou.delclientv3.gui.newgui;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.oldgui.util.FontUtil;
import com.github.dellixou.delclientv3.modules.core.settings.SettingsManager;
import com.github.dellixou.delclientv3.utils.RenderUtils;
import com.github.dellixou.delclientv3.utils.gui.ImageProcessor;
import com.github.dellixou.delclientv3.utils.gui.SettingsArrow;
import com.github.dellixou.delclientv3.modules.core.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.utils.ColorUtils;
import com.github.dellixou.delclientv3.utils.misc.Reference;
import com.github.dellixou.delclientv3.utils.gui.animations.LinearAnimation;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.shaders.misc.DrawHelper;
import net.minecraft.client.Minecraft;
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

import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnable;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class NewClickGUI extends GuiScreen {

    // Animations
    public LinearAnimation popAnim;
    public LinearAnimation searchBoxFocus;
    public LinearAnimation settingsPanelAnim;
    private Map<Category, LinearAnimation> categoryAlphaAnimations;

    // Fonts
    private static final GlyphPageFontRenderer font = GlyphPageFontRenderer.create("Arial", 24, true, true, true);

    // Images
    private ResourceLocation BACK_IMAGE = ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/back.png"), 512, 512);
    private ResourceLocation SEARCH_IMAGE = ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/search.png"), 512, 512);
    private ResourceLocation ARROW_IMAGE = ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/arrowd.png"), 512, 512);
    public ResourceLocation SETTINGS_IMAGE = ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/settings.png"), 512, 512);

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
    private NewModuleButton currentOpenModule;

    // Settings
    private boolean isBackSettingHover = false;
    public boolean isSettingOpen = false;
    private Color settingBGColor = new Color(47, 47, 56, 255);
    private Setting currentSettingHover;
    public Map<String, Setting> currentOptionHovered;
    private final SettingsArrow settingsArrow = new SettingsArrow();

    // Settings Scroll
    public float settingsScrollAmount = 0f;
    private float settingsTargetScrollAmount = 0f;
    public float settingsCurrentScrollAmount = 0f;
    private float settingsMaxScroll = 0f;
    private float settingsLerpSpeedScroll = 0.2f;

    private int settingsScrollBarWidth = 2;
    private int settingsScrollBarHeight;
    private int settingsScrollBarX;
    private int settingsScrollBarY;
    private int settingsScrollBarThumbHeight;
    private int settingsScrollBarThumbY;
    private boolean isDraggingSettingsScrollBar = false;

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
        int rectWidth = 170; // 160
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
            if(mod.getId().equals("click_gui") || mod.getId().equals("remote_control")){
                blackListMod.add(newModuleButton);
            }
            buttons.get(i).checkBlacklist();
            i++;
        }

        categoryIcon = new HashMap<>();
        for(Category cat : Category.values()){
            if(cat.equals(Category.FLOOR7)){
                categoryIcon.put(cat, ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/dungeon.png"), 512, 512));
            }else if(cat.equals(Category.MOVEMENT)){
                categoryIcon.put(cat, ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/movement.png"), 512, 512));
            }else if(cat.equals(Category.RENDER)){
                categoryIcon.put(cat, ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/render.png"), 512, 512));
            }else if(cat.equals(Category.MACRO)){
                categoryIcon.put(cat, ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/macro.png"), 512, 512));
            }else if(cat.equals(Category.PLAYER)){
                categoryIcon.put(cat, ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/player.png"), 512, 512));
            }else if(cat.equals(Category.MISC)){
                categoryIcon.put(cat, ImageProcessor.resizeAndLoadTexture(new ResourceLocation("textures/misc.png"), 512, 512));
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

        categoryAlphaAnimations = new HashMap<>();
        for (Category category : Category.values()) {
            LinearAnimation animation = new LinearAnimation(0, 255, 200, false);
            if (category == selectedCategory) {
                animation.AnimationUpdateValue(255, 255, 200, true);
            }
            categoryAlphaAnimations.put(category, animation);
        }

        settingsScrollAmount = 0f;
        settingsTargetScrollAmount = 0f;
        settingsCurrentScrollAmount = 0f;
        currentOpenModule = null;

    }

    /*
     * When gui is closed.
     */
    @Override
    public void onGuiClosed() {
        // End Blur
        if(mc.entityRenderer.isShaderActive()){
            mc.entityRenderer.stopUseShader();
        }

        for(NewModuleButton button : buttons){
            if(button.isSettingOpen){
                button.isClosingSettings = true;
            }
        }

        currentCategoryHover = null;
        currentSettingHover = null;
        currentOpenModule = null;
        currentScrollAmount = 0;

        for(Setting set : DelClient.settingsManager.getSettings()){
            if(set.isSlider()) set.dragging = false;
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
        // Hover reset
        currentCategoryHover = null;
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
        int buttonXPos = 185; //175
        int sideMargin = 10;

        // -------------------------------------------- BACKGROUND --------------------------------------------

        float scale = popAnim.getAnimationValue();
        scale = easeOutCirc(scale);
        GlStateManager.pushMatrix();
        GlStateManager.translate(screenWidth / 2, screenHeight / 2, 0);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-screenWidth / 2, -screenHeight / 2, 0);

        // Draw the rectangle
        DrawHelper.drawRoundedBlurredRect(rectX-7.5f, rectY+BGHeight-7.5f+15, BGWidth+15, BGHeight+15, 10, 20, new Color(21, 21, 21, 150));
        DrawHelper.drawRoundedRect(rectX, rectY+BGHeight, BGWidth, BGHeight, 3, new Color(21, 21, 26, 255));


        // Little line between two parts
        DrawHelper.drawRoundedRect(rectX+BGWidth-buttonXPos-sideMargin , rectY+10+(BGHeight-20), 2, BGHeight-20, 1, new Color(36, 36, 36, 123));


        // -------------------------------------------- RIGHT SIDE --------------------------------------------

        currentScrollAmount = lerp(currentScrollAmount, targetScrollAmount, lerpSpeedScroll * partialTicks);
        scrollAmount = currentScrollAmount;

        // Draw Modules Buttons
        glEnable(GL11.GL_SCISSOR_TEST);
        scissor(rectX + BGWidth - buttonXPos - sideMargin * 2, rectY + 5, buttonXPos + sideMargin, BGHeight - 20);

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
            if(searchText.isEmpty()){
                if(button.mod.getCategory().equals(selectedCategory)){
                    button.y = (buttonH + buttonP) * visibleIndex;
                    visibleIndex++;
                }
            }else if(button.mod.getName().toLowerCase().contains(searchText.toLowerCase())){
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

        GlStateManager.pushMatrix();
        GlStateManager.translate(rectX + 3, rectY+BGHeight-font.getFontHeight()+6, 0);
        GlStateManager.scale(0.4f, 0.4f, 0.4f);
        font.drawString("DelClient-beta"+ Reference.VERSION, 0, 0, new Color(230, 230, 230, 200).getRGB(), false);
        GlStateManager.popMatrix();

        // -------------------------------------------- SETTINGS --------------------------------------------

        // TODO: Combo box pour choisir les animations : ease ..

        for(NewModuleButton button : buttons){
            if(button.isSettingOpen || button.isClosingSettings){
                if (currentOpenModule != button) {
                    settingsScrollAmount = 0f;
                    settingsTargetScrollAmount = 0f;
                    settingsCurrentScrollAmount = 0f;
                    currentOpenModule = button;
                }
                button.isSettingOpen = true;
                // Settings Panel
                float Sx = rectX + BGWidth - buttonXPos - sideMargin * 2;
                float Sy = rectY;
                float Swidth = buttonXPos + sideMargin * 2;
                float Sheight = BGHeight;

                float scaleX = button.isClosingSettings ? 1 - settingsPanelAnim.getAnimationValue() : settingsPanelAnim.getAnimationValue();
                //scaleX = easeOutBack(scaleX);
                scaleX = easeOutQuart(scaleX);

                // Background
                GlStateManager.pushMatrix();
                GlStateManager.translate(Sx + Swidth, Sy, 0);
                GlStateManager.scale(scaleX, 1, 1);
                GlStateManager.translate(-(Sx + Swidth), -Sy, 0);

                DrawHelper.drawRoundedBlurredRect(Sx, Sy + Sheight-7.5f, Swidth, Sheight-15, 3, 15, new Color(37, 37, 43, 255));
                DrawHelper.drawRoundedRect(Sx, Sy + Sheight, Swidth, Sheight, 3, new Color(37, 37, 43, 255));

                // Title Mod
                GlStateManager.pushMatrix();
                GlStateManager.translate(Sx + 25, Sy + font.getFontHeight()/2f*0.75f+3f, 0);
                GlStateManager.scale(0.75, 0.75, 0.75);
                font.drawString("§l" + button.mod.getName(), 0, 0, new Color(255, 255, 255, 255).getRGB(), true);
                GlStateManager.popMatrix();

                settingsScrollBarHeight = BGHeight - 40;
                settingsScrollBarX = (int) (Sx + Swidth - settingsScrollBarWidth-5);
                settingsScrollBarY = (int) (Sy + 25);
                updateSettingsScrollBarThumb();

                // Back button
                isBackSettingHover = isBackSettingOver(mouseX, mouseY, Sx, Sy, 32, 32);
                Color c = isBackSettingHover ? new Color(215, 215, 215, 176) : new Color(167, 167, 167, 176);

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                if(isBackSettingHover){
                    glColor4f(0.7f, 0.7f, 0.7f, 1);
                }else{
                    glColor4f(1f, 1f, 1f, 1);
                }
                Minecraft.getMinecraft().getTextureManager().bindTexture(BACK_IMAGE);
                drawModalRectWithCustomSizedTexture((int)Sx+1, (int)Sy+2, 0, 0, 24, 23, 24, 23);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();

                if(button.mod.hasSettings()){
                    // Calculate the height for settings
                    float totalSettingsHeight = calculateTotalSettingsHeight(button);
                    settingsMaxScroll = Math.max(0, totalSettingsHeight - (Sheight - 40));

                    // Using lerp for mouse wheel
                    settingsCurrentScrollAmount = lerp(settingsCurrentScrollAmount, settingsTargetScrollAmount, settingsLerpSpeedScroll * partialTicks);
                    settingsScrollAmount = settingsCurrentScrollAmount;

                    // Apply scissor
                    glEnable(GL11.GL_SCISSOR_TEST);
                    scissorSettings(Sx-50, Sy + 25, Swidth+50, Sheight - 30);

                    // Draw settings
                    drawSettings(button, Sx, Sy + 25 - settingsScrollAmount, Swidth-10, mouseX, mouseY, partialTicks);

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
                    currentOpenModule = null;
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
            totalHeight += 20; // Hauteur pour le titre de la catégorie
            for (Setting setting : categorySettings) {
                if (setting.isCheck()) {
                    totalHeight += 17;
                } else if (setting.isSlider()) {
                    totalHeight += 23;
                } else if (setting.isText()) {
                    totalHeight += 23;
                } else if (setting.isCombo()) {
                    totalHeight += 23;
                }
            }
            totalHeight += 10; // Espace entre les catégories
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
        RenderUtils.renderIcon(SEARCH_IMAGE, 8, new Color(125, 125, 125, 255), (int)searchBoxX+4, (int)searchBoxY+4);

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
     * Util to remove _ and put upper case title category name.
     */
    public static String formatCategoryName(String input) {
        String[] words = input.replace('_', ' ').split("\\s+");

        return Arrays.stream(words)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /*
     * Draw settings.
     */
    private void drawSettings(NewModuleButton button, float x, float y, float width, int mouseX, int mouseY, float partialTicks) {
        int offset = 10;

        for (String category : DelClient.settingsManager.getAllSettingsCategory(button.mod)) {
            List<Setting> categorySettings = DelClient.settingsManager.getSettingsByModAndCategory(button.mod, category);

            int categoryHeight = 0;

            for (Setting cat : categorySettings){
                if(cat.isCheck()) categoryHeight += 17;
                if(cat.isSlider()) categoryHeight += 23;
                if(cat.isCombo()) categoryHeight += 17;
                if(cat.isText()) categoryHeight += 31;
            }

            // Draw the backgrounds for each categories
            DrawHelper.drawRoundedRect(x + 3, y + offset + categoryHeight, width - 6, categoryHeight, 2, settingBGColor);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 3, y + offset - 9, 0);
            GlStateManager.scale(0.6, 0.6, 0.6);
            String categoryName = formatCategoryName(category);
            font.drawString(categoryName + " : ", 0, 0, new Color(234, 234, 234, 220).getRGB(), false);
            GlStateManager.popMatrix();

            // Draw parameters
            for (Setting setting : categorySettings) {
                // CHECK
                if (setting.isCheck()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 6, y + offset + font.getFontHeight()/2*0.6, 0);
                    GlStateManager.scale(0.6, 0.6, 0.6);
                    font.drawString(setting.getName(), 0, 0, new Color(227, 227, 227, 200).getRGB(), false);
                    GlStateManager.popMatrix();

                    Color checkColor;
                    if (setting.getValBoolean()) {
                        checkColor = ColorUtils.getClickGUIColor().darker();
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

                // SLIDER
                if (setting.isSlider()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 6, y + offset + font.getFontHeight()/2*0.6, 0);
                    GlStateManager.scale(0.6, 0.6, 0.6);
                    font.drawString(setting.getName(), 0, 0, new Color(227, 227, 227, 200).getRGB(), false);
                    GlStateManager.popMatrix();

                    String displayval = "" + Math.round(setting.getValDouble() * 100D) / 100D;

                    int padding = 8;
                    float displayValX = (float)(x + width - font.getStringWidth(displayval) * (0.55) - padding);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(displayValX, y + offset + font.getFontHeight()/2*0.6, 0);
                    GlStateManager.scale(0.55, 0.55, 0.55);
                    font.drawString(displayval, 0, 0, new Color(227, 227, 227, 200).getRGB(), false);
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
                    DrawHelper.drawRoundedRect(x + 6, y + offset + 16f, setting.currentPercentBar * (width - 12), 2, 1, ColorUtils.getClickGUIColor().darker());

                    // Circle
                    Color c = isHoveringSetting(mouseX, mouseY, x+6, y+offset+12f, width-12, 5, setting) ? ColorUtils.getClickGUIColor().darker().darker() : ColorUtils.getClickGUIColor().darker();
                    if (setting.getValDouble() > setting.getMin() && setting.getValDouble() < setting.getMax()) {
                        DrawHelper.drawCircle(x + 6 + setting.currentPercentBar * (width - 12), y+offset+15, 2, c);
                    }

                    // OFFSET
                    offset += 23;
                }

                // COMBO
                if (setting.isCombo()) {
                    float settingHeight = 17;
                    float rectHeight = 10;
                    float arrowSize = 10;
                    float margin = 4;
                    float minRectWidth = 30;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 6, y + offset + settingHeight/2 - font.getFontHeight()*0.6f/2, 0);
                    GlStateManager.scale(0.6, 0.6, 0.6);
                    font.drawString(setting.getName(), 0, 0, new Color(227, 227, 227, 200).getRGB(), false);
                    GlStateManager.popMatrix();

                    String currentValue = setting.getValString().substring(0, 1).toUpperCase() + setting.getValString().substring(1);

                    float textWidth = font.getStringWidth(currentValue) * 0.6f;
                    float rectWidth = Math.max(minRectWidth, textWidth + arrowSize + margin * 3);

                    float rectX = x + width - rectWidth - margin;
                    float rectY = y + offset + (settingHeight - rectHeight) / 2;

                    boolean hovered = isHoveringSetting(mouseX, mouseY, rectX, rectY, rectWidth, rectHeight, setting);
                    Color color = hovered ? new Color(227, 227, 227, 200) : Color.WHITE;
                    float alpha = hovered ? 0.4f : 0.7f;

                    DrawHelper.drawRoundedRect(rectX, rectY + rectHeight, rectWidth, rectHeight, 4, new Color(28, 30, 31, 255));

                    float arrowX = rectX + margin - 2.5f;
                    float arrowY = (rectY + (rectHeight - arrowSize) / 2) - 2;

                    int alphaInt = (int) Math.min(255, alpha*255);

                    int iconSize = 12;
                    int centerX = (int)arrowX + 1 + iconSize / 2;
                    int centerY = (int)arrowY + 1 + iconSize / 2;

                    // Expanded rotation
                    float rotateTarget = setting.isExpanded ? 90 : 0;
                    float interpolatedRotation = setting.currentRotationArrow + (rotateTarget - setting.currentRotationArrow) * partialTicks * 0.1f;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(centerX + 1, centerY + 0.5f, 0);
                    GlStateManager.rotate(interpolatedRotation, 0, 0, 1); // Rotation to make hover animations
                    GlStateManager.translate(-iconSize / 2, -iconSize / 2, 0);
                    RenderUtils.renderIcon(ARROW_IMAGE, iconSize, new Color(255, 255, 255, alphaInt), 0, 0);
                    GlStateManager.popMatrix();

                    // Expanded rotation set rotation
                    setting.currentRotationArrow = interpolatedRotation;

                    float textX = rectX + arrowSize + margin * 2 - 3;
                    float textY = rectY + (rectHeight - font.getFontHeight() * 0.6f) / 2;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(textX, textY, 0);
                    GlStateManager.scale(0.6, 0.6, 0.6);
                    font.drawString(currentValue, 0, 0, color.getRGB(), false);
                    GlStateManager.popMatrix();

                    // If combo settings expanded
                    if(setting.isExpanded){
                        float ay = 5;
                        for (String sld : setting.getOptions()) {
                            String optionString = formatCategoryName(sld.toLowerCase());


                            ay += font.getFontHeight()+0.5f;

                            float optionTextWidth = font.getStringWidth(optionString) * 0.6f; // Scaled width
                            float optionX = x + 100 + (width - 103 - optionTextWidth) / 2;
                            float optionY = y + offset + ay + 3;

                            Color optionColorText =  isHoveringOption(mouseX, mouseY, x + 100, y + offset + ay, width - 103, font.getFontHeight(), sld, setting) ? new Color(227, 227, 227, 200) : Color.WHITE;

                            float targetOpacity = setting.getValString().equalsIgnoreCase(sld) ? 255f : 0f;

                            float interpolatedOpacity = setting.selectedOpacity + (targetOpacity - setting.selectedOpacity) * partialTicks * 0.05f;

                            Color optionColorSelected = new Color(ColorUtils.getClickGUIColor().darker().getRed(),
                                                                    ColorUtils.getClickGUIColor().darker().getGreen(),
                                                                    ColorUtils.getClickGUIColor().darker().getBlue(), Math.min(255, (int)interpolatedOpacity));

                            DrawHelper.drawRoundedRect(x + 100.5f, y + offset + font.getFontHeight() + ay + 1, width - 104, font.getFontHeight() + 1, 2, new Color(32, 33, 38, 255));
                            if(setting.getValString().equalsIgnoreCase(sld)){
                                DrawHelper.drawRoundedRect(x + 100.5f, y + offset + font.getFontHeight() + ay + 1, width - 104, font.getFontHeight() + 1, 2, optionColorSelected);
                                setting.selectedOpacity = interpolatedOpacity;
                            }

                            GlStateManager.pushMatrix();
                            GlStateManager.translate(optionX, optionY, 0);
                            GlStateManager.scale(0.6, 0.6, 0.6);
                            font.drawString(optionString, 0, 0, optionColorText.getRGB(), true);
                            GlStateManager.popMatrix();
                        }

                        DrawHelper.drawRoundedRectOutline(x + 100, y + offset + ay + 1 + font.getFontHeight(),width - 103, ay - 4, 3,1.3f, new Color(63, 63, 69, 255));

                        offset += ay - 10;
                    }

                    // OFFSET
                    offset += settingHeight;
                }

                // TEXT
                if (setting.isText()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 6, y + offset + font.getFontHeight()/2*0.6, 0);
                    GlStateManager.scale(0.6, 0.6, 0.6);
                    font.drawString(setting.getName(), 0, 0, new Color(227, 227, 227, 200).getRGB(), false);
                    GlStateManager.popMatrix();

                    // Background
                    Color hoveredColor = isHoveringSetting(mouseX, mouseY, x+6, y+offset+16f, width-12, 14, setting) ? new Color(255, 255, 255, 200) : new Color(227, 227, 227, 200);
                    Color focusColor = setting.isFocused ? new Color(0x404040) : new Color(0x232323);
                    DrawHelper.drawRoundedRect(x + 6, y + offset + 16f + 12, width-12, 14, 1, focusColor);

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 7, y + offset + font.getFontHeight()/2*0.6 + 13, 0);
                    GlStateManager.scale(0.6, 0.6, 0.6);
                    font.drawString(setting.getValString(), 0, 0, hoveredColor.getRGB(), false);
                    GlStateManager.popMatrix();

                    if (setting.isFocused && System.currentTimeMillis() % 1000 > 500) {
                        DrawHelper.drawRoundedRect(x + 8 + font.getStringWidth(setting.getValString())*0.6f + 1, y + offset + 16 + 10, 1, 14 - 10 + 5, 1, Color.WHITE);
                    }

                    // OFFSET
                    offset += 30;
                }
            }
            offset += 17; // Space between parameters
        }

        drawSettingsScrollBar(mouseX, mouseY, partialTicks);
    }

    /*
     * Draw settings scroll bar.
     */
    private void drawSettingsScrollBar(int mouseX, int mouseY, float partialTicks) {
        // Fond de la barre de défilement
        DrawHelper.drawRoundedRect(settingsScrollBarX, settingsScrollBarY + settingsScrollBarHeight, settingsScrollBarWidth, settingsScrollBarHeight, 1, new Color(66, 66, 66, 200));

        // Barre de défilement
        DrawHelper.drawRoundedRect(settingsScrollBarX, settingsScrollBarThumbY + settingsScrollBarThumbHeight, settingsScrollBarWidth, settingsScrollBarThumbHeight, 1, new Color(80, 80, 80, 255));

        // Logique de glissement
        if (isDraggingSettingsScrollBar) {
            int mouseYRelative = mouseY - settingsScrollBarY;
            float percentage = (float) mouseYRelative / settingsScrollBarHeight;
            settingsTargetScrollAmount = percentage * settingsMaxScroll;
            settingsTargetScrollAmount = MathHelper.clamp_float(settingsTargetScrollAmount, 0, settingsMaxScroll);
        }

        updateSettingsScrollBarThumb();
    }

    /*
     * Update height scroll bar.
     */
    private void updateSettingsScrollBarThumb() {
        float contentHeight = calculateTotalSettingsHeight(currentOpenModule);
        float visibleRatio = Math.min(1, (BGHeight - 40) / contentHeight);
        settingsScrollBarThumbHeight = Math.max(20, (int) (settingsScrollBarHeight * visibleRatio));
        float scrollPercentage = settingsCurrentScrollAmount / settingsMaxScroll;
        settingsScrollBarThumbY = settingsScrollBarY + (int) ((settingsScrollBarHeight - settingsScrollBarThumbHeight) * scrollPercentage);
    }

    /*
     * Draw category button
     */
    private void drawCategoryButton(Category category, float x, float y, float width, float height, int mouseX, int mouseY, float partialTicks){

        LinearAnimation alphaAnimation = categoryAlphaAnimations.get(category);

        boolean isHovering = isHoveringCategory(mouseX, mouseY, x, y, width, height, category);

        if (isHovering && Mouse.isButtonDown(0) && !selectedCategory.equals(category)) {
            alphaAnimation.AnimationUpdateValue(alphaAnimation.getAnimationValue(), 255, 200, true);
            categoryAlphaAnimations.get(selectedCategory).AnimationUpdateValue(categoryAlphaAnimations.get(selectedCategory).getAnimationValue(), 0, 200, true);
            selectedCategory = category;
        }

        int alpha = Math.max(0, Math.min(255, (int) alphaAnimation.getAnimationValue()));

        if (selectedCategory.equals(category) || alpha > 0) {
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            Color glowColor = new Color(ColorUtils.getClickGUIColor().getRed(), ColorUtils.getClickGUIColor().getGreen(), ColorUtils.getClickGUIColor().getBlue(), alpha);
            DrawHelper.drawGlow(x, y + height, (int)width, (int)height, 5, glowColor);
            DrawHelper.drawRoundedRect(x, y + height, width, height, 3, new Color(28, 30, 31, alpha));
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
        }

        int iconX = (int) (x + 5);
        int iconY = (int) (y + (height - 12) / 2);

        ResourceLocation iconCat = categoryIcon.get(category);

        String text = "§l" + category.name().substring(0, 1).toUpperCase() + category.name().substring(1).toLowerCase();

        Color c = new Color(168, 168, 168, 255);
        if(isHoveringCategory(mouseX, mouseY, x, y, width, height, category)){
            if(Mouse.isButtonDown(0)){ // Check if left mouse button is pressed
                selectedCategory = category;
            }
            c = Color.WHITE;
        }

        RenderUtils.renderIcon(iconCat, 12, new Color(179, 179, 179, 255), iconX, iconY);

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

    /*
     * Mouse clicked.
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for(Setting setting : DelClient.settingsManager.getSettings()){
            if(setting.isText()){
                setting.isFocused = false;
            }
        }
        // COMBO SETTINGS
        if (currentOptionHovered != null && !currentOptionHovered.isEmpty()) {
            Map.Entry<String, Setting> entry = currentOptionHovered.entrySet().iterator().next();
            String option = entry.getKey();
            Setting setting = entry.getValue();
            setting.setValString(option);
            setting.selectedOpacity = 0f;
        }

        // CLICK SETTINGS
        if(currentSettingHover != null){
            isSearchBoxFocused = false;
            if(currentSettingHover.isCheck()){
                currentSettingHover.setClicked(!currentSettingHover.isClicked());
            }
            if(currentSettingHover.isSlider()){
                currentSettingHover.dragging = true;
            }
            if(currentSettingHover.isCombo()){
                currentSettingHover.isExpanded = !currentSettingHover.isExpanded;
            }
            if(currentSettingHover.isText()){
                currentSettingHover.isFocused = !currentSettingHover.isFocused;
            }
        }
        // CLICK SCROLL BAR SETINGS
        if (mouseButton == 0 && isMouseOverSettingsScrollBar(mouseX, mouseY)) {
            isDraggingSettingsScrollBar = true;
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
            //searchBoxWidth = 70; // Reset to initial width
            //searchBoxFocus = new LinearAnimation(searchBoxWidth, 70, 100, true);
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
        if (isDraggingSettingsScrollBar) {
            isDraggingSettingsScrollBar = false;
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
        for (Setting setting : DelClient.settingsManager.getSettings()){
            if(setting.isText() && setting.isFocused){
                if (keyCode == Keyboard.KEY_ESCAPE){
                    setting.isFocused = false;
                }
                if (keyCode == Keyboard.KEY_BACK && !setting.getValString().isEmpty()) {
                    setting.setValString(setting.getValString().substring(0, setting.getValString().length() - 1));
                } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                    setting.setValString(setting.getValString() + typedChar);
                }
            }
        }

        if (isSearchBoxFocused) {
            if (keyCode == Keyboard.KEY_ESCAPE){
                isSearchBoxFocused = false;
            }
            if (keyCode == Keyboard.KEY_BACK && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
            } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                searchText += typedChar;
            }
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

    private boolean isHoveringOption(int mouseX, int mouseY, float x, float y, float width, float height, String option, Setting setting) {
        boolean hover = mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;

        if (hover) {
            // Créer une nouvelle HashMap avec l'option et le Setting
            Map<String, Setting> optionInfo = new HashMap<>();
            optionInfo.put(option, setting);

            currentOptionHovered = optionInfo;
        } else if (currentOptionHovered != null && currentOptionHovered.containsKey(option)) {
            currentOptionHovered = null;
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

    private boolean isMouseOverSettingsScrollBar(int mouseX, int mouseY) {
        return mouseX >= settingsScrollBarX && mouseX <= settingsScrollBarX + settingsScrollBarWidth &&
                mouseY >= settingsScrollBarY && mouseY <= settingsScrollBarY + settingsScrollBarHeight;
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

