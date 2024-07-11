package com.github.dellixou.delclientv3.events.networks;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.*;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
class PacketReceivedEvent extends Event {
    public final Packet<?> packet;

    public PacketReceivedEvent(Packet<?> packet) {
        this.packet = packet;
    }
}

@Cancelable
class PacketSentEvent extends Event {
    public final Packet<?> packet;

    public PacketSentEvent(Packet<?> packet) {
        this.packet = packet;
    }
}


@Cancelable
class PacketEntityEvent extends Event {
    public final S14PacketEntity packet;

    public PacketEntityEvent(S14PacketEntity packet) {
        this.packet = packet;
    }
}

@Cancelable
class PacketParticleEvent extends Event {
    public final S2APacketParticles packet;

    public PacketParticleEvent(S2APacketParticles packet) {
        this.packet = packet;
    }
}

@Cancelable
class PacketEntityEquipmentEvent extends Event {
    public final S04PacketEntityEquipment packet;

    public PacketEntityEquipmentEvent(S04PacketEntityEquipment packet) {
        this.packet = packet;
    }
}

@Cancelable
class PacketTeamsEvent extends Event {
    public final S3EPacketTeams packet;

    public PacketTeamsEvent(S3EPacketTeams packet) {
        this.packet = packet;
    }
}

@Cancelable
class PacketSpawnObjectEvent extends Event {
    public final S0EPacketSpawnObject packet;

    public PacketSpawnObjectEvent(S0EPacketSpawnObject packet) {
        this.packet = packet;
    }
}

@Cancelable
class PacketSpawnMobEvent extends Event {
    public final S0FPacketSpawnMob packet;

    public PacketSpawnMobEvent(S0FPacketSpawnMob packet) {
        this.packet = packet;
    }
}

@Cancelable
class PacketExplosionEvent extends Event {
    public final S27PacketExplosion packet;

    public PacketExplosionEvent(S27PacketExplosion packet) {
        this.packet = packet;
    }
}

@Cancelable
class PacketBlockPlaceEvent extends Event {
    public final C08PacketPlayerBlockPlacement packet;

    public PacketBlockPlaceEvent(C08PacketPlayerBlockPlacement packet) {
        this.packet = packet;
    }
}

@Cancelable
class PacketTransactionEvent extends Event {
    public final S32PacketConfirmTransaction packet;

    public PacketTransactionEvent(S32PacketConfirmTransaction packet) {
        this.packet = packet;
    }
}