package com.github.dellixou.delclientv3.events;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.lwjgl.Sys;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TickEvent {

    @SubscribeEvent
    public void onTick(net.minecraftforge.fml.common.gameevent.TickEvent event) {
        // User route
        DelClient.userRoute.onUpdate();
    }
}