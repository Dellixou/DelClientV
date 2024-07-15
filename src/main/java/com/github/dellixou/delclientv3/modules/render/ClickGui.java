package com.github.dellixou.delclientv3.modules.render;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import org.lwjgl.input.Keyboard;

public class ClickGui extends Module {

    public ClickGui() {
        super("Click GUI", Keyboard.KEY_M, Category.RENDER, false, "click_gui");
    }

    @Override
    public void setup() {
        //ArrayList<String> options = new ArrayList<String>();
        //options.add("Default");
        //DelClient.settingsManager.rSetting(new Setting("Design", this, "Default", options, "click_gui_design"));
        DelClient.settingsManager.rSetting(new Setting("Color Red", this, 255, 0, 255, true, "click_gui_red", "colors"));
        DelClient.settingsManager.rSetting(new Setting("Color Green", this, 135, 0, 255, true, "click_gui_green", "colors"));
        DelClient.settingsManager.rSetting(new Setting("Color Blue", this, 173, 0, 255, true, "click_gui_blue", "colors"));
        DelClient.settingsManager.rSetting(new Setting("Sound", this, false, "click_gui_sound", "misc"));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        mc.displayGuiScreen(DelClient.newClickGUI);
        toggle();
    }
}