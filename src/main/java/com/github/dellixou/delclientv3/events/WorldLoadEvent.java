package com.github.dellixou.delclientv3.events;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.utils.misc.VersionVerifier;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.IOException;

public class WorldLoadEvent {

    private boolean firstTimeLoaded = false;
    private boolean stopEnableModules = false;
    private boolean worldJustLoaded = false;
    private boolean alreadyCheckedUUID = false;

    /*
     * Function loaded after joining any world/server.
     */
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if(!firstTimeLoaded){
            firstTimeLoaded = true;
        }
        this.worldJustLoaded = true;
    }

    /*
     * Function player tick instantly after joined world to prevent zero tick.
     */
    @SubscribeEvent()
    public void onTick(TickEvent.PlayerTickEvent event){
        if(this.worldJustLoaded) {
            this.worldJustLoaded = false;
            DelClient.fileManager.loadRoutes();
        }
        if(this.firstTimeLoaded && !stopEnableModules){
            stopEnableModules = true;
            if(!VersionVerifier.isLastVersion()){
                DelClient.sendWarning("&cYou're not using the latest version for DelClient!");
            }
            DelClient.fileManager.loadMods();
        }
    }

    /*
     * Disconnect player from server.
     */
    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event){
        firstTimeLoaded = false;
        stopEnableModules = false;
        for(Module module : ModuleManager.getModules()){
            module.setToggled(false);
        }
    }

}
