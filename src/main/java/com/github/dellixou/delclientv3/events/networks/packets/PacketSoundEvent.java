package com.github.dellixou.delclientv3.events.networks.packets;

import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PacketSoundEvent extends Event {

    public final S29PacketSoundEffect packet;

    public PacketSoundEvent(S29PacketSoundEffect packet) {
        this.packet = packet;
    }

}

