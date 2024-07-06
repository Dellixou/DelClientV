package com.github.dellixou.delclientv3.events.gui;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OpenLeapMenuEvent {

    public String playerToLeap = null;

    @SubscribeEvent
    public void onLeapMenu(GuiOpenEvent event) {
        if(event.gui != null){
            if(event.gui instanceof GuiChest){
                    Thread thread = new Thread(() -> {
                        try {
                            Thread.sleep(200);
                            GuiChest guiChest = (GuiChest) event.gui;
                            ContainerChest container = (ContainerChest) guiChest.inventorySlots;
                            IInventory lowerInventory = container.getLowerChestInventory();
                            String inventoryName = lowerInventory.getDisplayName().getFormattedText();
                            if(inventoryName.contains("Spirit Leap")){
                                DelClient.sendDebug("It's Spirit Leap Menu");
                                if(lowerInventory.getSizeInventory() <= 0) return;
                                for (int i = 0; i < lowerInventory.getSizeInventory(); i++) {
                                    ItemStack itemStack = lowerInventory.getStackInSlot(i);
                                    if (itemStack != null && itemStack.getDisplayName().contains(playerToLeap)) {
                                        int windowId = container.windowId;
                                        Minecraft.getMinecraft().playerController.windowClick(
                                                windowId, // The window ID of the container
                                                i,       // The slot ID to click on
                                                0,       // The mouse button clicked (0 for left click)
                                                0,       // The type of click (0 for click)
                                                Minecraft.getMinecraft().thePlayer // The player
                                        );
                                        MinecraftForge.EVENT_BUS.unregister(this);
                                        return;
                                    }
                                }
                                MinecraftForge.EVENT_BUS.unregister(this);
                                return;
                            }
                            MinecraftForge.EVENT_BUS.unregister(this);
                        } catch (InterruptedException e) {
                            DelClient.sendDebug(e.getMessage());
                        }
                    });
                    thread.start();
            }

        }
    }

}
