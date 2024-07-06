package com.github.dellixou.delclientv3.events;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LivingUpdate {

    /**
     * Function loaded while player is living
     **/

    // Mains Parameters
    private Minecraft mc;
    private ModuleManager mm;
    private int SPRINT_KEY;

    /**
     * On world load
     **/
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        mc = Minecraft.getMinecraft();
        mm = DelClient.moduleManager;
        SPRINT_KEY = mc.gameSettings.keyBindSprint.getKeyCode();
    }

    /**
     * Function loaded while player is living
     **/
    @SubscribeEvent
    public void onLiving(LivingEvent.LivingUpdateEvent event){
        if(!(event.entity instanceof EntityPlayerSP)) return;
        // Updates modules
        ModuleManager.onUpdate();
    }
}