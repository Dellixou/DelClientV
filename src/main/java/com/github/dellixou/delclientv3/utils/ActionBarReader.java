package com.github.dellixou.delclientv3.utils;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ActionBarReader {

    private static Field recordPlayingField;
    static {
        try {
            recordPlayingField = ReflectionHelper.findField(GuiIngame.class, "recordPlaying", "field_73838_g");
            recordPlayingField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class PlayerStats {
        public String health;
        public String maxHealth;
        public String defense;
        public String maxDefense;
        public String mana;
        public String maxMana;
    }

    public static String getActionBarText() {
        Minecraft mc = Minecraft.getMinecraft();
        GuiIngame guiIngame = mc.ingameGUI;
        try {
            return (String) recordPlayingField.get(guiIngame);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static PlayerStats parseActionBar(String actionBar) {
        PlayerStats stats = new PlayerStats();

        // Patterns plus flexibles, tolérants aux espaces
        Pattern healthPattern = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*/\\s*(\\d+(?:[.,]\\d+)?)\\s*❤");
        Pattern defensePattern = Pattern.compile("(\\d+)\\s*❈\\s*Defense");
        Pattern manaPattern = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)\\s*✎\\s*Mana");

        Matcher healthMatcher = healthPattern.matcher(actionBar);
        if (healthMatcher.find()) {
            stats.health = healthMatcher.group(1).replace(",", "");
            stats.maxHealth = healthMatcher.group(2).replace(",", "");
        } else {
            stats.health = stats.maxHealth = "N/A";
        }

        Matcher defenseMatcher = defensePattern.matcher(actionBar);
        if (defenseMatcher.find()) {
            stats.defense = defenseMatcher.group(1);
        } else {
            stats.defense = "N/A";
        }

        Matcher manaMatcher = manaPattern.matcher(actionBar);
        if (manaMatcher.find()) {
            stats.mana = manaMatcher.group(1);
            stats.maxMana = manaMatcher.group(2);
        } else {
            stats.mana = stats.maxMana = "N/A";
        }

        return stats;
    }

}
