package com.github.dellixou.delclientv3.events;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TickEvent {

    @SubscribeEvent
    public void onTick(net.minecraftforge.fml.common.gameevent.TickEvent event) {
        DelClient.userRoute.onUpdate();
    }

}
