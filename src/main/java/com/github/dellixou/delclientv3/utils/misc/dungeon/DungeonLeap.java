package com.github.dellixou.delclientv3.utils.misc.dungeon;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.gui.OpenLeapMenuEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;

public class DungeonLeap {

    static Minecraft mc = Minecraft.getMinecraft();

    public static void leapToWithClass(DungeonClass dungeonClass){
        // SETUP ALL THINGS
        DungeonClassDetector dungeonClassDetector = new DungeonClassDetector();
        OpenLeapMenuEvent openLeapMenuEvent = new OpenLeapMenuEvent();
        dungeonClassDetector.updatePlayerClasses();
        openLeapMenuEvent.playerToLeap = DungeonClassDetector.searchClassPlayerName(dungeonClass);
        for (Map.Entry<String, String> entry : dungeonClassDetector.getPlayerClassMap().entrySet()) {
            String playerName = entry.getKey();
            String playerClass = entry.getValue();
            String className = playerClass.substring(0, playerClass.indexOf(" "));
            DelClient.sendDebug("&aPlayer Name : " + playerName);
            DelClient.sendDebug("&cPlayer Class : " + playerClass);
            DelClient.sendDebug("&eClass Name : " + className);
        }
        MinecraftForge.EVENT_BUS.register(openLeapMenuEvent);

        // CLICKS IN GAME
        Thread thread = new Thread(() -> {
            try {
                Minecraft.getMinecraft().thePlayer.inventory.currentItem = findLeap();
                ItemStack itemHand = mc.thePlayer.getHeldItem();
                Thread.sleep(30);
                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                //mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemHand, new BlockPos(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ()), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);
            } catch (InterruptedException ignored) { }
        });
        thread.start();
    }

    public static void leapToWithName(String playerName){
        DungeonClassDetector dungeonClassDetector = new DungeonClassDetector();
        OpenLeapMenuEvent openLeapMenuEvent = new OpenLeapMenuEvent();
        dungeonClassDetector.updatePlayerClasses();
        openLeapMenuEvent.playerToLeap = playerName;
        MinecraftForge.EVENT_BUS.register(openLeapMenuEvent);
    }

    /**
     * Find the fishing rod in hot bar.
     */
    public static int findLeap(){
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if(itemStack != null && (itemStack.getDisplayName().contains("Spirit Leap") || itemStack.getDisplayName().contains("Infinileap")) ){
                return i;
            }
        }
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            DelClient.sendDebug(itemStack.getDisplayName());
        }
        return 0;
    }


}
