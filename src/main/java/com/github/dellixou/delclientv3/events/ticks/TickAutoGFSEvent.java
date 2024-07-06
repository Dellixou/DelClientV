package com.github.dellixou.delclientv3.events.ticks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class TickAutoGFSEvent {

    private int tickCounter = 0;
    private int pearlRefreshCounter = 0;
    private boolean didPearl = false;

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.player != null) {
            tickCounter++;
            pearlRefreshCounter++;
            if (tickCounter >= 20 && !didPearl) {
                tickCounter = 0; // Reset the tick counter

                checkEnderPearl(Minecraft.getMinecraft().thePlayer);
            }
            if(pearlRefreshCounter >= 10000){
                pearlRefreshCounter = 0;
                didPearl = false;
            }
        }
    }

    public void checkEnderPearl(EntityPlayer player) {

        boolean hasEnderPearls = false;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() == Items.ender_pearl) {
                hasEnderPearls = true;
                break;
            }
        }

        if (!hasEnderPearls) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/gfs ender_pearl 16");
            didPearl = true;
        }
    }
}
