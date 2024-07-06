package com.github.dellixou.delclientv3.events.chats;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.ticks.TickAutoPre4Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatAutoPre4Event {

    // Event Tick Final
    private final TickAutoPre4Event tickAutoPre4Event = new TickAutoPre4Event();

    /**
     * Chat Received Event
     */
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        int playerX = player.getPosition().getX();
        int playerY = player.getPosition().getY();
        int playerZ = player.getPosition().getZ();
        boolean isAt4 = (playerX >= 62 && playerX <= 65 && playerZ >= 34 && playerZ <= 37 && playerY == 127);
        if(message.contains("[BOSS] Goldor: Who dares trespass into my domain?") && isAt4){
            DelClient.sendChatToClient("&eStarting Auto Pre 4! Don't move buddy...");
            MinecraftForge.EVENT_BUS.register(tickAutoPre4Event);
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

}
