package com.github.dellixou.delclientv3.modules.misc;

import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import org.lwjgl.input.Keyboard;

public class RemoteControl extends Module {

    /**
     * Constructor to initialize the GFS module.
     */
    public RemoteControl() {
        super("Remote Control", Keyboard.KEY_0, Category.MISC, true, "remote_control");
    }

    /**
     * Sets up initial settings for the AutoFish module.
     */
    public void setup(){
    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable(){
        super.onEnable();
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable(){
        super.onDisable();
    }

}
