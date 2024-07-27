package com.github.dellixou.delclientv3.events.gui;

import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class OpenClickGUIEvent {

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // As soon as a tick occurs, show the screen and stop listening for events
        ModuleManager.getModuleById("click_gui").onEnable();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

}
