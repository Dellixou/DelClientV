package com.github.dellixou.delclientv3.events;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.utils.Reference;
import com.github.dellixou.delclientv3.utils.misc.UUIDVerifier;
import com.github.dellixou.delclientv3.utils.misc.VersionVerifier;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
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
    private UUIDVerifier uuidVerifier;

    /**
     * Function loaded after joining any world/server
     **/

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if(!firstTimeLoaded){
            firstTimeLoaded = true;
            uuidVerifier = new UUIDVerifier();
        }
        this.worldJustLoaded = true;
        DelClient.instance.chatServerNameEvent.isListening = true;
    }


    @SubscribeEvent()
    public void onTick(TickEvent.PlayerTickEvent event){
        if(this.worldJustLoaded) {
            this.worldJustLoaded = false;
            DelClient.fileManager.loadRoutes();
        }
        if(this.firstTimeLoaded && !stopEnableModules){
            stopEnableModules = true;
            DelClient.fileManager.loadMods();
        }
        if(!alreadyCheckedUUID){
            try {
                if(false){
                    uuidVerifier.fetchAllowedUUIDs(Minecraft.getMinecraft().thePlayer);
                }

                DelClient.instance.setIsAuthorized(true);
                alreadyCheckedUUID = true;
                /**
                boolean isAuthorized = uuidVerifier.fetchAllowedUUIDs(Minecraft.getMinecraft().thePlayer).contains(Minecraft.getMinecraft().thePlayer.getUniqueID().toString());
                alreadyCheckedUUID = true;
                if(isAuthorized){
                    DelClient.sendChatToClient("&7Authorization : &aAuthorized!");
                    DelClient.instance.setIsAuthorized(true);
                    if(!VersionVerifier.isLastVersion(Reference.VERSION)){
                        DelClient.sendChatToClient("&7Version : &cYour version is outdated! Please download the : " + VersionVerifier.getCurrentVersion().get(0));
                    }
                }else{
                    DelClient.sendChatToClient("&7Authorization : &cDenied!");
                    DelClient.instance.setIsAuthorized(false);
                    if(!VersionVerifier.isLastVersion(Reference.VERSION)){
                        DelClient.sendChatToClient("&7Version : &cYour version is outdated! Please download the : " + VersionVerifier.getCurrentVersion().get(0));
                    }
                    // CRASH
                    //Minecraft.getMinecraft().crashed(new CrashReport("Access denied to DelClient", null));
                    //Minecraft.getMinecraft().shutdownMinecraftApplet();
                 }
                 **/
            }catch (IOException e){
                DelClient.sendChatToClient(e.getMessage());
                DelClient.sendChatToClient("&aPLEASE CONTACT DELMELON WITH THIS ERROR!");
                alreadyCheckedUUID = true;
            }
        }

    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event){
        firstTimeLoaded = false;
        stopEnableModules = false;
        alreadyCheckedUUID = false;
        DelClient.instance.setIsAuthorized(false);
        for(Module module : ModuleManager.getModules()){
            module.setToggled(false);
        }
    }

}
