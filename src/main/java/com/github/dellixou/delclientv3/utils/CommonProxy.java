package com.github.dellixou.delclientv3.utils;

import com.github.dellixou.delclientv3.events.LivingUpdate;
import com.github.dellixou.delclientv3.events.TickEvent;
import com.github.dellixou.delclientv3.events.WorldLoadEvent;
import com.github.dellixou.delclientv3.events.chats.TestChat;
import com.github.dellixou.delclientv3.events.draw.DrawBlockHighlightEvent;
import com.github.dellixou.delclientv3.events.draw.RenderGameOverlay;
import com.github.dellixou.delclientv3.events.draw.RenderWorldLastEvent;
import com.github.dellixou.delclientv3.events.keys.KeyInputEvent;
import com.github.dellixou.delclientv3.events.misc.MousePositionChatEvent;
import com.github.dellixou.delclientv3.gui.GuiIngameHook;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

    /**
     * Common registers / Client & Server
     **/

    // References
    public static GuiIngameHook guiIngameHook = new GuiIngameHook();

    public void registerRenders() {
        // Renders !!
    }


    /**
     * Function loaded after joining any world/server
     **/
    public void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new WorldLoadEvent());
        MinecraftForge.EVENT_BUS.register(new LivingUpdate());
        MinecraftForge.EVENT_BUS.register(new KeyInputEvent());
        guiIngameHook.startTime = System.currentTimeMillis();
        MinecraftForge.EVENT_BUS.register(guiIngameHook);
        MinecraftForge.EVENT_BUS.register(new MousePositionChatEvent());
        MinecraftForge.EVENT_BUS.register(new TickEvent());
        MinecraftForge.EVENT_BUS.register(new DrawBlockHighlightEvent());
        MinecraftForge.EVENT_BUS.register(new RenderWorldLastEvent());
        MinecraftForge.EVENT_BUS.register(new RenderGameOverlay());
        MinecraftForge.EVENT_BUS.register(new TestChat());
    }

}
