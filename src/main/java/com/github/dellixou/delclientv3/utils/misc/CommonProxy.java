package com.github.dellixou.delclientv3.utils.misc;

import com.github.dellixou.delclientv3.events.LivingUpdate;
import com.github.dellixou.delclientv3.events.TickEvent;
import com.github.dellixou.delclientv3.events.WorldLoadEvent;
import com.github.dellixou.delclientv3.events.draw.DrawBlockHighlightEvent;
import com.github.dellixou.delclientv3.events.draw.RenderWorldLastEvent;
import com.github.dellixou.delclientv3.events.keys.KeyInputEvent;
import com.github.dellixou.delclientv3.events.misc.MainMenuEventHandler;
import com.github.dellixou.delclientv3.events.misc.MousePositionChatEvent;
import com.github.dellixou.delclientv3.gui.GuiIngameHook;
import com.github.dellixou.delclientv3.utils.SkyblockUtils;
import com.github.dellixou.delclientv3.utils.TickUtils;
import com.github.dellixou.delclientv3.utils.movements.RotationUtils;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

    /**
     * Common registers / Client & Server
     **/

    // References
    public static GuiIngameHook guiIngameHook = new GuiIngameHook();

    public void registerRenders() {
        // EMPTY
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
        MinecraftForge.EVENT_BUS.register(new SkyblockUtils());
        MinecraftForge.EVENT_BUS.register(new TickUtils());
        MinecraftForge.EVENT_BUS.register(new RotationUtils());
        MinecraftForge.EVENT_BUS.register(new MainMenuEventHandler());
    }

}
