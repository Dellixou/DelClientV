package com.github.dellixou.delclientv3.utils;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InventoryUtils {

    public static void swapItemHotBar(String itemName){
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if(itemStack != null && (itemStack.getDisplayName().contains(itemName))){
                Minecraft.getMinecraft().thePlayer.inventory.currentItem = i;
                return;
            }
        }
        DelClient.sendWarning("The item : &7" + itemName + "&c is not found on your inventory hot bar!");
    }

    public static void swapItemIndex(int index){
        Minecraft.getMinecraft().thePlayer.inventory.currentItem = index;
    }

    @SideOnly(Side.CLIENT)
    public static void closeCurrentScreen() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft.getMinecraft().thePlayer.closeScreen();
            });
        }
    }

}
