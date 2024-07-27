package com.github.dellixou.delclientv3.modules.render;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import org.lwjgl.input.Keyboard;


public class FOVChanger extends Module{

    /**
     * Change fov
     **/
    public FOVChanger() {
        super("FOV Changer", Keyboard.KEY_O, Category.RENDER, true, "fov_changer");
    }

    @Override
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Field of view", this, 130, 1, 179, true, "fov_changer_value", "global"));
    }

    public void onUpdate(){
        if(this.isToggled() && mc.gameSettings.fovSetting != (float) DelClient.settingsManager.getSettingById("fov_changer_value").getValDouble()){
            double fovChanger = DelClient.settingsManager.getSettingById("fov_changer_value").getValDouble();
            mc.gameSettings.fovSetting = (float) fovChanger;
        }
    }

    public void onEnable(){
        if(this.isToggled()){
            double fovChanger = DelClient.settingsManager.getSettingById("fov_changer_value").getValDouble();
            mc.gameSettings.fovSetting = (float) fovChanger;
            super.onEnable();
        }
    }

    @Override
    public void onDisable(){
        mc.gameSettings.fovSetting = 90F;
        super.onDisable();
    }

}
