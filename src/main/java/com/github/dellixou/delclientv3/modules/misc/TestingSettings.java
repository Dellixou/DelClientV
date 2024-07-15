package com.github.dellixou.delclientv3.modules.misc;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.ticks.TickAutoGFSEvent;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public class TestingSettings extends Module {

    /**
     * Constructor to initialize the AutoFish module.
     */
    public TestingSettings() {
        super("TEST", Keyboard.KEY_I, Category.PLAYER, true, "test_set");
    }


    /**
     * Sets up initial settings for the AutoFish module.
     */
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("1", this, false, "1", "group1"));
        DelClient.settingsManager.rSetting(new Setting("2", this, false, "2", "group1"));
        DelClient.settingsManager.rSetting(new Setting("3", this, false, "3", "group1"));
        DelClient.settingsManager.rSetting(new Setting("4", this, false, "4", "group2"));
        DelClient.settingsManager.rSetting(new Setting("5", this, false, "5", "group2"));
        DelClient.settingsManager.rSetting(new Setting("Delay", this, 1000, 100, 1000, true, "auto_fish_rod_throw", "global"));
        DelClient.settingsManager.rSetting(new Setting("Delay", this, 1000, 100, 1000, true, "auto_fish_rod_throw", "global"));
        DelClient.settingsManager.rSetting(new Setting("Delay", this, 1000, 100, 1000, true, "auto_fish_rod_throw", "global"));
        DelClient.settingsManager.rSetting(new Setting("Delay", this, 1000, 100, 1000, true, "auto_fish_rod_throw", "global"));
    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable(){

        BlockPos start = mc.thePlayer.getPosition();

        BlockPos end = new BlockPos(100, 70, 100);


        super.onEnable();
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable(){
        super.onDisable();
    }


    /**
     * Called periodically to update the module's state.
     */
    @Override
    public void onUpdate() {
        if (this.isToggled() && mc.thePlayer != null) {

        }
    }

}
