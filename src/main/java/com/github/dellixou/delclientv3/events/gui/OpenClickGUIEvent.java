package com.github.dellixou.delclientv3.events.gui;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.utils.misc.UUIDVerifier;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class OpenClickGUIEvent {

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // As soon as a tick occurs, show the screen and stop listening for events
        try{
            if(DelClient.instance.getIsAuthorized()){
                DelClient.sendDebug("YES");
                ModuleManager.getModuleById("click_gui").onEnable();
            }else{
                DelClient.sendDebug("NO");
                Minecraft.getMinecraft().crashed(new CrashReport("Access denied to DelClient", null));
            }
        }catch (Exception ignored) { }
        /**
        try{
            UUIDVerifier uuidVerifier = new UUIDVerifier();
            boolean isAuthorized = uuidVerifier.fetchAllowedUUIDs(Minecraft.getMinecraft().thePlayer).contains(Minecraft.getMinecraft().thePlayer.getUniqueID().toString());
            if(!isAuthorized){ // Here !
                ModuleManager.getModuleById("click_gui").onEnable();
            }else{
                Minecraft.getMinecraft().crashed(new CrashReport("Access denied to DelClient", null));
            }
        }catch (Exception ignored) { }
         **/
        MinecraftForge.EVENT_BUS.unregister(this);
    }

}
