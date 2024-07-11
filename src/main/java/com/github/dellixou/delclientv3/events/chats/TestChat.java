package com.github.dellixou.delclientv3.events.chats;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.networks.packets.PacketChatEvent;
import com.github.dellixou.delclientv3.events.networks.packets.PacketSoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TestChat {

    @SubscribeEvent
    public void onChat(PacketChatEvent event){
        //DelClient.sendDebug(event.packet.getMessage());
    }

    @SubscribeEvent
    public void onSound(PacketSoundEvent event){
        //System.out.print(event.packet.getSoundName());
    }

}
