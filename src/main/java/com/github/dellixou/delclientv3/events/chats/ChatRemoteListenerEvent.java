package com.github.dellixou.delclientv3.events.chats;

import com.github.dellixou.delclientv3.utils.ColorUtils;
import com.github.dellixou.delclientv3.utils.remote.DiscordBotInstance;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatRemoteListenerEvent {

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if(event.type == 0){
            String message = ColorUtils.cleanMinecraftText(event.message.getUnformattedText());
            ChatManager.addMessage(message);
        }
    }

    public static class ChatManager {
        private static final int MAX_MESSAGES = 10;
        private static List<String> lastMessages = new ArrayList<>();

        public static void addMessage(String message) {
            lastMessages.add(message);
            if (lastMessages.size() > MAX_MESSAGES) {
                lastMessages.remove(0);
            }
        }

        public static String formatMessagesAsCodeBlock() {
            StringBuilder sb = new StringBuilder("```\n");
            for (String message : lastMessages) {
                sb.append("- " + message).append("\n");
            }
            sb.append("```");
            return sb.toString();
        }
    }

}
