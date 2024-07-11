package com.github.dellixou.delclientv3.events.networks.packets;

import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PacketChatEvent  extends Event {

    public final C01PacketChatMessage packet;

    public PacketChatEvent(C01PacketChatMessage packet) {
        this.packet = packet;
    }

}
