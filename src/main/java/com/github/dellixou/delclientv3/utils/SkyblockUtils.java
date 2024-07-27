package com.github.dellixou.delclientv3.utils;

import com.github.dellixou.delclientv3.utils.enums.SkyblockZone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collection;

public class SkyblockUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static SkyblockZone currentZone = SkyblockZone.NULL;
    private static String currentArea = "Unknown Sub Area"; // The forest for example
    private static String actionBarText = "";

    public static SkyblockZone getCurrentZone(){
        return currentZone;
    }

    public static String getCurrentArea(){
        return currentArea;
    }

    public static String getZoneNameFromEnum(SkyblockZone zone){
        String zoneName = "";
        switch (zone) {
            case HUB:
                zoneName = "Hub";
                break;
            case PARK:
                zoneName = "The Park";
                break;
            case PRIV_ISLAND:
                zoneName = "Private Island";
                break;
            case NULL:
                zoneName = "Unknown Zone";
                break;
            default:
                break;
        }
        return zoneName;
    }

    public static SkyblockZone getZoneFromName(String zoneName){
        SkyblockZone skyblockZone = SkyblockZone.NULL;
        switch (zoneName.toLowerCase()) {
            case "hub":
                skyblockZone = SkyblockZone.HUB;
                break;
            case "the park":
                skyblockZone = SkyblockZone.PARK;
                break;
            case "private island":
                skyblockZone = SkyblockZone.PRIV_ISLAND;
            default:
                break;
        }
        return skyblockZone;
    }

    public static String getActionbar() {
        return ColorUtils.cleanMinecraftText(ActionBarReader.getActionBarText());
    }

    public static ActionBarReader.PlayerStats getPlayerStatsActionBar(){
        String actionBarText = getActionbar();
        return ActionBarReader.parseActionBar(actionBarText);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) {return;}

        // Get tab list with players
        Collection<NetworkPlayerInfo> players = mc.getNetHandler().getPlayerInfoMap();

        for (NetworkPlayerInfo playerInfo : players) {
            IChatComponent displayNameComponent = playerInfo.getDisplayName();
            if (displayNameComponent != null) {

                String displayName = displayNameComponent.getUnformattedText();

                if (displayName.startsWith("Area: ")) {

                    String newZoneString = displayName.substring(6);
                    SkyblockZone skyblockZone = getZoneFromName(newZoneString);

                    if (!skyblockZone.equals(currentZone)) {
                        currentZone = skyblockZone;
                    }

                    break;
                }
            }
        }
    }

}
