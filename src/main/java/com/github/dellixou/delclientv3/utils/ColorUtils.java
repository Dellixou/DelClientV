package com.github.dellixou.delclientv3.utils;

import com.github.dellixou.delclientv3.DelClient;
import org.apache.commons.lang3.Validate;

import java.awt.*;

public class ColorUtils {

    /**
     * Transform "&e" to color in message
     **/
    public static final char COLOUR_CHAR = '\u00A7';

    public static String translateAlternativeColourCode(char alternateColourCode, String string){
        Validate.notNull(string, "Cannot translate null text");

        char[] b = string.toCharArray();
        for (int i = 0; i < b.length - 1; i++){
            if(b[i] == alternateColourCode && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1){
                b[i] = ColorUtils.COLOUR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);

            }
        }
        return  new String(b);
    }

    public static String chat(String string){
        return ColorUtils.translateAlternativeColourCode('&', string);
    }

    public static Color lerpColor(Color color1, Color color2, float t) {
        t = Math.max(0, Math.min(1, t)); // Clamp t between 0 and 1

        float r = color1.getRed() + t * (color2.getRed() - color1.getRed());
        float g = color1.getGreen() + t * (color2.getGreen() - color1.getGreen());
        float b = color1.getBlue() + t * (color2.getBlue() - color1.getBlue());
        float a = color1.getAlpha() + t * (color2.getAlpha() - color1.getAlpha());

        return new Color(
                Math.round(r) / 255f,
                Math.round(g) / 255f,
                Math.round(b) / 255f,
                Math.round(a) / 255f
        );
    }

    public static Color getClickGUIColor(){
        return new Color((int) DelClient.instance.settingsManager.getSettingByName("Color Red").getValDouble(), (int)DelClient.instance.settingsManager.getSettingByName("Color Green").getValDouble(), (int)DelClient.instance.settingsManager.getSettingByName("Color Blue").getValDouble());
    }

    public static Color rainbowEffect(long offset, float fade) {
        float hue = (float) (System.nanoTime() + offset) / 1.0E10F % 1.0F;
        long color = Long.parseLong(Integer.toHexString(Integer.valueOf(Color.HSBtoRGB(hue, 1.0F, 1.0F)).intValue()), 16);
        Color c = new Color((int) color);
        return new Color(c.getRed()/255.0F * fade, c.getGreen()/255.0F * fade, c.getBlue()/255.0F * fade, c.getAlpha()/255.0F);
    }

    public static String cleanMinecraftText(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder cleaned = new StringBuilder();
        boolean skipNext = false;

        for (int i = 0; i < input.length(); i++) {
            if (skipNext) {
                skipNext = false;
                continue;
            }

            char current = input.charAt(i);

            if (current == '§' && i + 1 < input.length()) {
                skipNext = true;
            } else {
                cleaned.append(current);
            }
        }

        return cleaned.toString();
    }

}