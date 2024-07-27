package com.github.dellixou.delclientv3.utils;

import com.github.dellixou.delclientv3.DelClient;
import com.google.gson.*;

public class JsonUtils {

    public static Gson gson = new Gson();
    public static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    public static JsonParser jsonParser = new JsonParser();

    public static String extractFromJson(String message, String toExtract) {
        try {
            message = message.trim();
            if (message.startsWith("{") && message.endsWith("}")) {
                String jsonPart = message;

                if (isValidJson(jsonPart)) {
                    JsonObject jsonObject = new JsonParser().parse(jsonPart).getAsJsonObject();
                    if (jsonObject.has(toExtract)) {
                        DelClient.instance.currentPlayerLocation = jsonObject.get(toExtract).getAsString();
                        return jsonObject.get(toExtract).getAsString();
                    }
                }
            }
        } catch (Exception ignored) { }
        return null;
    }

    public static boolean isValidJson(String json) {
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

}
