package com.github.dellixou.delclientv3.utils.dungeon;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.utils.enums.DungeonClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonClassDetector {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Map<String, String> playerClassMap = new HashMap<>();
    // Updated regex to handle optional Skyblock level and symbols
    private static final Pattern pattern = Pattern.compile("^\\[(\\d+)\\] (?:\\[\\w+\\] )*(\\w+) (?:.)*?\\((\\w+)(?: (\\w+))*\\)$");

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld != null && mc.thePlayer != null) {
            updatePlayerClasses();
        }
    }

    public void updatePlayerClasses() {
        try {
            playerClassMap.clear();

            for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                if (playerInfo.getDisplayName() != null) {
                    String displayName = playerInfo.getDisplayName().getFormattedText();
                    Matcher matcher = pattern.matcher(StringUtils.stripControlCodes(displayName));

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
}