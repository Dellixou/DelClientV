package com.github.dellixou.delclientv3.modules.floor7;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.chats.ChatAutoLeapsEvent;
import com.github.dellixou.delclientv3.events.gui.OpenLeapMenuEvent;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.misc.dungeon.DungeonClassDetector;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class AutoLeaps extends Module {

    /**
     * Constructor to initialize the AutoPre4 module.
     */
    public AutoLeaps() {
        super("Auto Leaps", Keyboard.KEY_0, Category.FLOOR7, true, "auto_leap");
    }

    // Fields for managing AutoLeaps
    public final ChatAutoLeapsEvent chatAutoLeapsEvent = new ChatAutoLeapsEvent();
    public final OpenLeapMenuEvent openLeapMenuEvent = new OpenLeapMenuEvent();
    public final DungeonClassDetector dungeonClassDetector = new DungeonClassDetector();

    String leapMethod = DelClient.settingsManager.getSettingById("auto_leap_method").getValString();
    String ee2Values = DelClient.settingsManager.getSettingById("auto_leap_ee2").getValString();
    String ee3Values = DelClient.settingsManager.getSettingById("auto_leap_ee3").getValString();
    String slingValues = DelClient.settingsManager.getSettingById("auto_leap_sling").getValString();

    public int currentStage = 1;
    public boolean finishedIsStageJob = false;

    /**
     * Sets up initial settings for the AutoPre4 module.
     */
    public void setup(){
        // LEAP METHOD
        ArrayList<String> leapSetting = new ArrayList<String>();
        leapSetting.add("Class");
        leapSetting.add("Name");
        DelClient.settingsManager.rSetting(new Setting("Leap Method", this, "Class", leapSetting, "auto_leap_method", "global"));
        // LEAP VALUES
        DelClient.settingsManager.rSetting(new Setting("Leap ee2", this, false, "auto_leap_ee2_b", "leap"));
        DelClient.settingsManager.rSetting(new Setting("ee2 Values", this, "Archer/a9.1s", "auto_leap_ee2", "leap"));

        DelClient.settingsManager.rSetting(new Setting("Leap ee3", this, false, "auto_leap_ee3_b", "leap"));
        DelClient.settingsManager.rSetting(new Setting("ee3 Values", this, "Healer/c", "auto_leap_ee3", "leap"));

        DelClient.settingsManager.rSetting(new Setting("Leap sling", this, false, "auto_leap_sling_b", "leap"));
        DelClient.settingsManager.rSetting(new Setting("Slingshot Values", this, "Mage/c", "auto_leap_sling", "leap"));

    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable(){
        chatAutoLeapsEvent.autoLeaps = this;
        DelClient.sendChatToClient("&7Started listen chat auto leaps!");
        MinecraftForge.EVENT_BUS.register(chatAutoLeapsEvent);
        MinecraftForge.EVENT_BUS.register(dungeonClassDetector);
        super.onEnable();
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable(){
        DelClient.sendChatToClient("&7Stopped listen chat auto leaps!");
        chatAutoLeapsEvent.stopListen();
        MinecraftForge.EVENT_BUS.unregister(chatAutoLeapsEvent);
        MinecraftForge.EVENT_BUS.unregister(openLeapMenuEvent);
        super.onDisable();
    }


    /**
     * Called periodically to update the module's state.
     */
    @Override
    public void onUpdate() {
    }
}
