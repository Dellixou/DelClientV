package com.github.dellixou.delclientv3.events.chats;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.ticks.TickAutoLeapsEvent;
import com.github.dellixou.delclientv3.modules.floor7.AutoLeaps;
import com.github.dellixou.delclientv3.utils.DungeonUtils;
import com.github.dellixou.delclientv3.utils.enums.DungeonClass;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatAutoLeapsEvent {

    // Event tick final
    private final TickAutoLeapsEvent tickDungeonAutoLeapsEvent = new TickAutoLeapsEvent();
    // Auto leap base
    public AutoLeaps autoLeaps;
    // Goldor messages
    private static final List<String> goldorMessageNW = new ArrayList<>();
    static {
        goldorMessageNW.add("[BOSS] Goldor: The little ants have a brain it seems.");
        goldorMessageNW.add("[BOSS] Goldor: I will replace that gate with a stronger one!");
    }

    /*
     * Chat Received Event
     */
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();;
        if(message.contains("[BOSS] Goldor: Who dares trespass into my domain?")){
            tickDungeonAutoLeapsEvent.autoLeaps = autoLeaps;
            MinecraftForge.EVENT_BUS.register(tickDungeonAutoLeapsEvent);
            DelClient.sendDebug("Goldor started!");
        }
        if(message.contains("[BOSS] Goldor: But you have nowhere to hide anymore!")){
            DelClient.sendDebug("Goldor defeated!");
            stopListen();
        }

        if(message.contains("(7/7)") || message.contains("(8/8)")){
            DelClient.sendDebug("Goldor stage adding!");

            if(autoLeaps.currentStage == 1 && DelClient.settingsManager.getSettingById("auto_leap_ee2_b").getValBoolean()){
                String ee2Value = DelClient.settingsManager.getSettingById("auto_leap_ee2").getValString();
                // Split the string by the '/' character
                String[] parts = ee2Value.split("/");
                // Get the part before the '/'
                String className = parts[0];
                DungeonUtils.leapToWithClass(DungeonClass.valueOf(className));
            }

            if(autoLeaps.currentStage == 2 && DelClient.settingsManager.getSettingById("auto_leap_ee3_b").getValBoolean()){
                String ee3Value = DelClient.settingsManager.getSettingById("auto_leap_ee3").getValString();
                // Split the string by the '/' character
                String[] parts = ee3Value.split("/");
                // Get the part before the '/'
                String className = parts[0];
                DungeonUtils.leapToWithClass(DungeonClass.valueOf(className));
            }

            if(autoLeaps.currentStage == 3 && DelClient.settingsManager.getSettingById("auto_leap_sling_b").getValBoolean()){
                String slingValue = DelClient.settingsManager.getSettingById("auto_leap_sling").getValString();
                // Split the string by the '/' character
                String[] parts = slingValue.split("/");
                // Get the part before the '/'
                String className = parts[0];
                DungeonUtils.leapToWithClass(DungeonClass.valueOf(className));
            }

            autoLeaps.currentStage += 1;
        }
    }

    /*
     * Stop all events
     */
    public void stopListen(){
        autoLeaps.currentStage = 1;
        MinecraftForge.EVENT_BUS.unregister(tickDungeonAutoLeapsEvent);
        tickDungeonAutoLeapsEvent.tickCounter = 0;
    }

}
