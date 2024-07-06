package com.github.dellixou.delclientv3.events.chats;

import com.github.dellixou.delclientv3.DelClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatServerNameEvent {

    public boolean isListening = false;

    /**
     * Chat Received Event
     */
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if(!isListening) return;

        String message = event.message.getUnformattedText();

        if (message.startsWith("{")) {
            String mode = extractModeFromJson(message);
            if(mode != null){
                isListening = false;
                if(DelClient.instance.currentPlayerLocation.equalsIgnoreCase("dungeon")){
                    //DelClient.routeLoader.loadRoutes();
                    //DelClient.sendChatToClient("&aLoaded all dungeon routes!");
                }else{
                    //DelClient.routeLoader.unloadRoute();
                }
            }
        }
    }

    private String extractModeFromJson(String message) {
        try {
            message = message.trim();

            if (message.startsWith("{") && message.endsWith("}")) {
                String jsonPart = message;

                if (isValidJson(jsonPart)) {
                    JsonObject jsonObject = new JsonParser().parse(jsonPart).getAsJsonObject();
                    if (jsonObject.has("map")) {
                        DelClient.instance.currentPlayerLocation = jsonObject.get("map").getAsString();
                        return jsonObject.get("map").getAsString();
                    } else {
                        DelClient.sendChatToClient("&cContact Delmelon, error can't load routes!");
                    }
                } else {
                    DelClient.sendChatToClient("&cContact Delmelon, error can't load routes! JSON not valid!");
                }
            } else {
                DelClient.sendChatToClient("&cContact Delmelon, error can't load routes! JSON not valid!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isValidJson(String json) {
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

}
