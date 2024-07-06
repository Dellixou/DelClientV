package com.github.dellixou.delclientv3.events.networks;

import com.github.dellixou.delclientv3.events.networks.packets.PacketChatEvent;
import com.github.dellixou.delclientv3.events.networks.packets.PacketSoundEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PacketHandler {
    private static final Map<Class<?>, Function<Packet<?>, Event>> packetReceivedMap;
    private static final Map<Class<?>, Function<Packet<?>, Event>> packetSentMap;

    static {
        packetReceivedMap = new HashMap<>();
        packetReceivedMap.put(C01PacketChatMessage.class, packet -> new PacketChatEvent((C01PacketChatMessage) packet));
        packetReceivedMap.put(S29PacketSoundEffect.class, packet -> new PacketSoundEvent((S29PacketSoundEffect) packet));

        packetSentMap = new HashMap<>();
        packetSentMap.put(C08PacketPlayerBlockPlacement.class, packet -> new PacketBlockPlaceEvent((C08PacketPlayerBlockPlacement) packet));
    }

    public static void handleReceivePacket(Packet<?> packet) {
        Function<Packet<?>, Event> eventCreator = packetReceivedMap.get(packet.getClass());
        if (eventCreator != null) {
            Event event = eventCreator.apply(packet);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }

    public static void handleSendPacket(Packet<?> packet) {
        Function<Packet<?>, Event> eventCreator = packetSentMap.get(packet.getClass());
        if (eventCreator != null) {
            Event event = eventCreator.apply(packet);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }
}
