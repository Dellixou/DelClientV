package com.github.dellixou.delclientv3.events.misc;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MousePositionChatEvent {

    @SubscribeEvent
    public void onGuiScreenDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        // Verification
        if(Minecraft.getMinecraft().thePlayer == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.ingameGUI.getChatGUI().getChatOpen()) {
            // Changes Values
            DelClient.instance.mouseXPos = event.mouseX;
            DelClient.instance.mouseYPos = event.mouseY;
        }
    }

}
