package com.github.dellixou.delclientv3.modules.misc;

import com.github.dellixou.delclientv3.events.ticks.TickAutoGFSEvent;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public class AutoGFS extends Module {

    /**
     * Constructor to initialize the AutoFish module.
     */
    public AutoGFS() {
        super("Auto GFS", Keyboard.KEY_0, Category.PLAYER, true, "auto_gfs");
    }

    // Fields for managing auto gfs
    TickAutoGFSEvent tickAutoGFSEvent = new TickAutoGFSEvent();


    /**
     * Sets up initial settings for the AutoFish module.
     */
    public void setup(){
    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable(){
        tickAutoGFSEvent.checkEnderPearl(Minecraft.getMinecraft().thePlayer);
        MinecraftForge.EVENT_BUS.register(tickAutoGFSEvent);
        super.onEnable();
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable(){
        MinecraftForge.EVENT_BUS.unregister(tickAutoGFSEvent);
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
