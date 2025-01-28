package com.github.dellixou.delclientv3.utils;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.gui.OpenLeapMenuEvent;
import com.github.dellixou.delclientv3.utils.enums.DungeonClass;
import com.github.dellixou.delclientv3.utils.dungeon.DungeonClassDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonUtils {

    static Minecraft mc = Minecraft.getMinecraft();
    private static final Map<String, String> playerClassMap = new HashMap<>();
    private static final Pattern classPatern = Pattern.compile("^\\[(\\d+)\\] (?:\\[\\w+\\] )*(\\w+) (?:.)*?\\((\\w+)(?: (\\w+))*\\)$");

    public void updatePlayerClasses() {
        try {
            playerClassMap.clear();

            for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                if (playerInfo.getDisplayName() != null) {
                    String displayName = playerInfo.getDisplayName().getFormattedText();
                    Matcher matcher = classPatern.matcher(StringUtils.stripControlCodes(displayName));

                    if (matcher.matches()) {
                        String skyblockLevel = matcher.group(1) != null ? matcher.group(1) : "N/A"; // Niveau de Skyblock (optionnel)
                        String playerName = matcher.group(2); // Nom du joueur
                        String playerClass = matcher.group(3); // Classe
                        String classLevel = matcher.group(4); // Niveau de classe

                        if (playerName != null && playerClass != null && classLevel != null) {
                            // Combinez la classe et le niveau de classe pour les stocker ensemble
                            String classWithLevel = playerClass + " " + classLevel;
                            playerClassMap.put(playerName, classWithLevel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            DelClient.sendDebug(e.getMessage());
        }
    }

    public Map<String, String> getPlayerClassMap() {
        return playerClassMap;
    }

    public static String searchClassPlayerName(DungeonClass dungeonClass){
        for (Map.Entry<String, String> entry : playerClassMap.entrySet()) {
            String playerName = entry.getKey();
            String playerClass = entry.getValue();
            String className = playerClass.substring(0, playerClass.indexOf(" "));
            if(DungeonClass.valueOf(className).equals(dungeonClass)){
                return playerName;
            }
        }
        return null;
    }

    public static void leapToWithClass(DungeonClass dungeonClass){
        DungeonClassDetector dungeonClassDetector = new DungeonClassDetector();
        OpenLeapMenuEvent openLeapMenuEvent = new OpenLeapMenuEvent();
        dungeonClassDetector.updatePlayerClasses();
        openLeapMenuEvent.playerToLeap = DungeonClassDetector.searchClassPlayerName(dungeonClass);
        for (Map.Entry<String, String> entry : dungeonClassDetector.getPlayerClassMap().entrySet()) {
            String playerName = entry.getKey();
            String playerClass = entry.getValue();
            String className = playerClass.substring(0, playerClass.indexOf(" "));
        }
        MinecraftForge.EVENT_BUS.register(openLeapMenuEvent);

        // CLICKS IN GAME
        Thread thread = new Thread(() -> {
            try {
                InventoryUtils.swapItemHotBar("Spirit Leap");
                InventoryUtils.swapItemHotBar("Infinileap");
                ItemStack itemHand = mc.thePlayer.getHeldItem();
                Thread.sleep(30);
                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
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

}
