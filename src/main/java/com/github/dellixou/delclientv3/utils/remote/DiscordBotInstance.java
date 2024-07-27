package com.github.dellixou.delclientv3.utils.remote;

import com.github.dellixou.delclientv3.events.chats.ChatRemoteListenerEvent;
import com.github.dellixou.delclientv3.utils.remote.enums.StatusRemote;
import com.github.dellixou.delclientv3.utils.remote.enums.StatusWebSocket;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.awt.*;


public class DiscordBotInstance {

    private static JDA jda;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static StatusRemote statusRemote = StatusRemote.NO_REMOTE;
    private static StatusWebSocket statusWebSocket = StatusWebSocket.CLOSED;
    private static final ChatRemoteListenerEvent chatRemoteListenerEvent = new ChatRemoteListenerEvent();

    public static boolean listeningGameChat = false;

    public static Color errorColor = new Color(134, 14, 14);
    public static Color successColor = new Color(30, 172, 41);

    public static boolean initBot(String token) throws InterruptedException {

        // Status for the bot
        String status = mc.thePlayer == null ? "Remote Control: ?" : "Remote Control: " + mc.thePlayer.getName();

        // JDA Create the bot
        jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .setActivity(Activity.customStatus(status))
                .addEventListeners(new DiscordEventListener())
                .build().awaitReady();

        return true;
    }

    public static StatusRemote getStatusRemote(){
        return statusRemote;
    }

    public static void setStatusRemote(StatusRemote status){
        statusRemote = status;
        if(status == StatusRemote.REMOTE){
            MinecraftForge.EVENT_BUS.register(chatRemoteListenerEvent);
        }else{
            try{
                MinecraftForge.EVENT_BUS.unregister(chatRemoteListenerEvent);
            }catch (Exception ignored) { }
        }
    }

    public static StatusWebSocket getStatusWebSocket(){
        return statusWebSocket;
    }

    public static void setStatusWebSocket(StatusWebSocket status){
        statusWebSocket = status;
    }

    public static JDA getJda(){
        return jda;
    }

    public static void setJda(JDA jda){
        DiscordBotInstance.jda = jda;
    }
}