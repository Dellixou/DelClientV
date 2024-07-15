package com.github.dellixou.delclientv3.modules.render;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.CommonProxy;
import org.lwjgl.input.Keyboard;


public class ModuleList extends Module{

    /**
     * Change fov
     **/
    public ModuleList() {
        super("Module List", Keyboard.KEY_O, Category.RENDER, true, "module_list");
    }

    @Override
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Text Rainbow", this, false, "module_list_text_rainbow", "visual"));
        DelClient.settingsManager.rSetting(new Setting("Background Rainbow", this, false, "module_list_bg_rainbow", "visual"));
        DelClient.settingsManager.rSetting(new Setting("Rainbow X", this, 8, 1, 10, true, "module_list_bg_rainbow_1", "visual"));
        DelClient.settingsManager.rSetting(new Setting("Rainbow Y", this, 7, 1, 10, true, "module_list_bg_rainbow_2", "visual"));
    }

    public void onEnable(){
        CommonProxy.guiIngameHook.enabled = true;
        super.onEnable();
    }

    @Override
    public void onDisable(){
        CommonProxy.guiIngameHook.enabled = false;
        super.onDisable();
    }

}
