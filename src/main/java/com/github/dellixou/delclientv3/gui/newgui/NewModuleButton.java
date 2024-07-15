package com.github.dellixou.delclientv3.gui.newgui;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.clickgui.Panel;
import com.github.dellixou.delclientv3.gui.clickgui.elements.Element;
import com.github.dellixou.delclientv3.gui.clickgui.elements.menu.ElementCheckBox;
import com.github.dellixou.delclientv3.gui.clickgui.elements.menu.ElementComboBox;
import com.github.dellixou.delclientv3.gui.clickgui.elements.menu.ElementSlider;
import com.github.dellixou.delclientv3.gui.clickgui.elements.menu.ElementWriteBox;
import com.github.dellixou.delclientv3.gui.clickgui.util.ColorUtil;
import com.github.dellixou.delclientv3.gui.newgui.settings.NewElement;
import com.github.dellixou.delclientv3.gui.newgui.settings.NewElementCheckBox;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.Color.AnimatedColor;
import com.github.dellixou.delclientv3.utils.Color.ColorUtils;
import com.github.dellixou.delclientv3.utils.gui.animations.LinearAnimation;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.misc.DrawHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;

public class NewModuleButton {

    // Important
    public NewClickGUI clickGUI;
    public boolean canClick = true;
    public boolean isSettingOpen = false;
    public boolean isClosingSettings = false;

    // Colors
    Color baseColor = new Color(37, 37, 45, 255);
    Color hoverColor = new Color(76, 76, 88, 255);

    // Animations
    LinearAnimation checkBoxClickAnim;
    private final AnimatedColor buttonColor = new AnimatedColor(baseColor, hoverColor, 200, false);
    private final AnimatedColor checkBoxColor = new AnimatedColor(new Color(64, 64, 64, 255), ColorUtil.getClickGUIColor(), 200, true);

    // Module info
    public Module mod;
    public ArrayList<NewElement> menuelements;
    public Panel parent;
    public double x;
    public double y;
    public double width;
    public double height;
    public int index = 0;
    private float originalX;
    private float bgX;
    private float bgY;

    // Hover info
    public boolean canHover;
    private boolean isHovering = false;
    private boolean isSettingHovering = false;

    // Fonts
    private static final GlyphPageFontRenderer font = GlyphPageFontRenderer.create("Arial", 28, true, true, true);

    /*
     * Constructor.
     */
    public NewModuleButton(Module mod, Panel panel, double x, double y, double width, double height) {
        this.mod = mod;
        this.parent = panel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.bgX = (float) (x + width - 40);
        this.bgY = (float) (y+(height)/2)-4.5f;
        if(!mod.hasSettings()){
            bgX+=10;
        }
        this.originalX = bgX + 4;

        updateCheckBoxState();
    }

    public void updateSettings(){
        menuelements = new ArrayList<NewElement>();
        if (DelClient.settingsManager.getSettingsByMod(mod) != null)
            for (Setting s : DelClient.settingsManager.getSettingsByMod(mod)) {
                if (s.isCheck()) {
                    menuelements.add(new NewElementCheckBox(this, s));
                } else if (s.isSlider()) {
                    //menuelements.add(new ElementSlider(this, s));
                } else if (s.isCombo()) {
                    //menuelements.add(new ElementComboBox(this, s));
                } else if(s.isText()) {
                    //menuelements.add(new ElementWriteBox(this, s));
                }
            }
    }

    /*
     * Check blacklist.
     */
    public void checkBlacklist(){
        if(clickGUI.blackListMod.contains(this)){
            canClick = false;
        }
    }

    /*
     * Draw screen.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks, double x, double y, double width, double height) {

        // Hover logic
        boolean isHovered = isMouseOverButton(mouseX, mouseY, x, width, y, height);
        if(isHovered) isHovering = true;
        else isHovering = false;

        boolean isToggle = mod.isToggled();

        Color checkColor = checkBoxColor.update(isToggle);
        Color currentColor = buttonColor.update(isHovered);

        DrawHelper.drawRoundedRect(x, y + height, width, height, 3, currentColor);

        // Text module name
        float textY = (float ) (y + (height - font.getFontHeight() * 0.6f) / 2);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 10, textY, 0);
        GlStateManager.scale(0.6f, 0.6f, 0.6f);
        font.drawString("§l"+mod.getName(), 0, 0, -1, true);
        GlStateManager.popMatrix();

        // Check box module
        float bgX = (float) (x+width-40);
        float bgY = (float) (y+(height)/2)-4.5f;
        if(!mod.hasSettings()){
            bgX += 10;
        }

        if(mod.hasSettings()){
            this.bgX = (float) (x + width - 40);
            this.bgY = (float) (y+(height)/2)-4.5f;
        }

        originalX = bgX + 4;

        if(canClick){
            // Background checkbox
            DrawHelper.drawRoundedRect(bgX, bgY+9, 18, 9, 4, checkColor);

            float checkBoxX = originalX;
            if(checkBoxClickAnim != null){
                checkBoxX = checkBoxClickAnim.getAnimationValue();
            }

            DrawHelper.drawCircle(checkBoxX, bgY+4.5f, 3, Color.WHITE);
        }

        if(mod.hasSettings()){ // HAVE SETTINGS ?
            // Settings button
            GlStateManager.pushMatrix();
            GlStateManager.translate(bgX+23, bgY+9, 0);
            GlStateManager.scale(0.3, 0.3, 0.3);
            GlStateManager.translate(-(bgX+23), -(bgY+9), 0);
            isSettingHovering = isMouseOverSettings(mouseX, mouseY, x, width, y, height);
            Color c = isSettingHovering ? new Color(255, 255, 255, 176) : new Color(215, 215, 215, 176);
            DrawHelper.drawRoundedTexture(new ResourceLocation("textures/settings.png"), bgX+23, bgY+9, 32, 32, 1, c);
            GlStateManager.popMatrix();
        }
    }

    /*
     * Update check box state.
     */
    public void updateCheckBoxState(){
        float endX = mod.isToggled() ? originalX + 10 : originalX;
        float startX = checkBoxClickAnim != null ? checkBoxClickAnim.getAnimationValue() : originalX;
        checkBoxClickAnim = new LinearAnimation(startX, endX, 200, true);
    }

    /*
     * Is hover
     */
    private boolean isMouseOverButton(int mouseX, int mouseY, double x, double width, double y, double height) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height && canHover && !isMouseOverSettings(mouseX, mouseY, x, width, y, height);
    }

    /*
     * Is hover settings.
     */
    private boolean isMouseOverSettings(int mouseX, int mouseY, double x, double width, double y, double height) {
        return mouseX >= bgX+23 && mouseX <= bgX+23+10 &&
                mouseY >= bgY-2 && mouseY <= bgY+10 && canHover && mod.hasSettings(); // SETTINGS
    }

    /*
     * Is clicked
     */
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(isSettingHovering && mouseButton == 0){
            isSettingOpen = true;
            clickGUI.settingsPanelAnim.reset();
            isClosingSettings = false;
            clickGUI.settingsPanelAnim.setIsDrawAnimation(true);
            clickGUI.isSettingOpen = true;
            clickGUI.settingsScrollAmount = 0f;
            clickGUI.settingsCurrentScrollAmount = 0f;
            for(Setting setting : DelClient.settingsManager.getSettingsByMod(mod)){
                if(setting.isCheck()){
                    setting.refreshCheckBox();
                }
                if(setting.isSlider()){
                    setting.currentPercentBar = 0f;
                }
            }
        }
        if(clickGUI.blackListMod.contains(this)){
            return false;
        }
        if(isHovering && mouseButton == 0){
            mod.toggle();
            updateCheckBoxState();
            return true;
        }
        return false;
    }
}
