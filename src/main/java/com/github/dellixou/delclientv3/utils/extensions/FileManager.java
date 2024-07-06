package com.github.dellixou.delclientv3.utils.extensions;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import com.github.dellixou.delclientv3.modules.render.ClickGui;
import com.github.dellixou.delclientv3.utils.misc.*;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.HashSet;
import java.util.Map;

public class FileManager {

    // Directory files
    public static File ROOT_DIR = new File("delclient");
    public static File ROUTES_DIR = new File("delclient/routes");
    public static File modules = new File(ROOT_DIR, "modules.json");
    private static UUIDVerifier uuidVerifier = new UUIDVerifier();

    /**
     * Initialize directories and handle modules
     **/
    public void init() {
        // Make Root Directory
        if (!ROOT_DIR.exists()) {
            ROOT_DIR.mkdirs();
        }
        if (!ROUTES_DIR.exists()) {
            ROUTES_DIR.mkdirs();
        }

        // Handles Module
        if (!modules.exists()) {
            saveMods();
        }

        //loadRoutes();
    }

    // -------------------------------------------------------------------------------------------------
    // Modules
    // -------------------------------------------------------------------------------------------------

    /**
     * Save Mods State
     **/
    public void saveMods() {
        try {
            JsonObject json = new JsonObject();
            for (Module mod : ModuleManager.getModules()) {

                // Toggle State
                JsonObject jsonMod = new JsonObject();
                jsonMod.addProperty("enabled", mod.isToggled());
                jsonMod.addProperty("key", mod.key);

                // Settings State
                for (Setting s : DelClient.settingsManager.getSettings()) {
                    if (s.getParentMod().getName().equalsIgnoreCase(mod.getName())) {
                        if (s.isCheck()) {
                            jsonMod.addProperty(s.getName(), s.getValBoolean());
                        }
                        if (s.isSlider()) {
                            jsonMod.addProperty(s.getName(), s.getValDouble());
                        }
                        if(s.isCombo()){
                            jsonMod.addProperty(s.getName(), s.getValString());
                        }
                        if(s.isText()){
                            jsonMod.addProperty(s.getName(), s.getValString());
                        }
                    }
                }
                // When All Added
                json.add(mod.getName(), jsonMod);
            }
            try (PrintWriter save = new PrintWriter(new FileWriter(modules))) {
                save.println(new GsonBuilder().setPrettyPrinting().create().toJson(json));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Blacklist of Mods
     **/
    private final HashSet<String> modBlackList = Sets.newHashSet(ClickGui.class.getName());

    /**
     * Check if a module is blacklisted
     **/
    public boolean isModBlackListed(Module m) {
        return modBlackList.contains(m.getClass().getName());
    }

    /**
     * Load Mods State
     **/
    public void loadMods() {
        try {
            BufferedReader load = new BufferedReader(new FileReader(modules));
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(load).getAsJsonObject();
            load.close();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                Module mod = DelClient.moduleManager.getModuleByName(entry.getKey());
                if (mod != null) {
                    JsonObject jsonModule = entry.getValue().getAsJsonObject();
                    boolean enabled;
                    try{
                        enabled = jsonModule.get("enabled").getAsBoolean();
                    }catch (Exception ignored) {
                        enabled = false;
                    }
                    int key;
                    try{
                        key = jsonModule.get("key").getAsInt();
                    }catch (Exception ignored) {
                        key = mod.getKey();
                    }

                    for (Setting s : DelClient.settingsManager.getSettings()) {
                        if (s.getParentMod().getName().equalsIgnoreCase(mod.getName())) {
                            if (s.isCheck() && jsonModule.has(s.getName())) {
                                boolean setting_enable = jsonModule.get(s.getName()).getAsBoolean();
                                s.setValBoolean(setting_enable);
                            }
                            if (s.isSlider() && jsonModule.has(s.getName())) {
                                double setting_val = jsonModule.get(s.getName()).getAsDouble();
                                s.setValDouble(setting_val);
                            }
                            if (s.isCombo() && jsonModule.has(s.getName())){
                                String setting_str = jsonModule.get(s.getName()).getAsString();
                                s.setValString(setting_str);
                            }
                            if (s.isText() && jsonModule.has(s.getName())){
                                String setting_str = jsonModule.get(s.getName()).getAsString();
                                s.setValString(setting_str);
                            }
                        }
                    }

                    // Set Key
                    mod.setKey(key);

                    // Skip blacklisted modules
                    if (isModBlackListed(mod)) {
                        continue;
                    }
                    //mod.setToggled(enabled); // Set the module state directly
                    if(enabled){
                        mod.toggle();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------------------------------
    // Routes
    // -------------------------------------------------------------------------------------------------

    /**
     * Save Routes Config
     **/
    public void saveRoutes() {
        try {
            UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");

            for (int i = 0; i < userRoute.routes.size(); i++) {
                Route route = userRoute.routes.get(i);
                JsonObject jsonRoute = new JsonObject();
                jsonRoute.addProperty("red", route.red);
                jsonRoute.addProperty("green", route.green);
                jsonRoute.addProperty("blue", route.blue);
                jsonRoute.addProperty("name", route.getName());

                JsonObject jsonWaypoints = new JsonObject();
                for (Waypoint waypoint : route.getWaypoints()) {
                    JsonObject jsonWaypoint = new JsonObject();
                    jsonWaypoint.addProperty("x", waypoint.getX());
                    jsonWaypoint.addProperty("y", waypoint.getY());
                    jsonWaypoint.addProperty("z", waypoint.getZ());
                    jsonWaypoint.addProperty("stopVelocity", waypoint.getStopVelocity());
                    jsonWaypoint.addProperty("useJump", waypoint.getUseJump());
                    jsonWaypoint.addProperty("look", waypoint.getLookOnly());
                    jsonWaypoint.addProperty("click", waypoint.getClick());
                    jsonWaypoint.addProperty("yaw", waypoint.getYaw());
                    jsonWaypoint.addProperty("pitch", waypoint.getPitch());
                    jsonWaypoint.addProperty("independent", waypoint.getIndependent());
                    jsonWaypoint.addProperty("routeItem", String.valueOf(waypoint.getRouteItem()));
                    jsonWaypoint.addProperty("edgeJump", waypoint.getEdgeJump());
                    jsonWaypoint.addProperty("bonzo", waypoint.getBonzo());
                    jsonWaypoint.addProperty("wait", waypoint.getWait());
                    jsonWaypoint.addProperty("time", waypoint.getTime());
                    jsonWaypoints.add(String.valueOf(route.getWaypoints().indexOf(waypoint)), jsonWaypoint);
                }
                jsonRoute.add("waypoints", jsonWaypoints);

                File routeFile = new File(ROUTES_DIR, route.getName() + ".json");
                PrintWriter save = new PrintWriter(new FileWriter(routeFile));
                save.println(JsonUtils.prettyGson.toJson(jsonRoute));
                save.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load Routes Config
     **/
    public void loadRoutes() {
        try {
            UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");
            userRoute.routes.clear();

            File[] routeFiles = ROUTES_DIR.listFiles((dir, name) -> name.endsWith(".json"));
            if (routeFiles != null) {
                for (File routeFile : routeFiles) {
                    BufferedReader load = new BufferedReader(new FileReader(routeFile));
                    JsonObject jsonRoute = (JsonObject) JsonUtils.jsonParser.parse(load);
                    load.close();

                    // Get the color properties
                    float red = jsonRoute.get("red").getAsFloat();
                    float green = jsonRoute.get("green").getAsFloat();
                    float blue = jsonRoute.get("blue").getAsFloat();
                    String name = jsonRoute.get("name").getAsString();
                    Route route = new Route(red, green, blue, name);

                    // Get the waypoints object
                    JsonObject jsonWaypoints = jsonRoute.getAsJsonObject("waypoints");

                    for (Map.Entry<String, JsonElement> waypointEntry : jsonWaypoints.entrySet()) {
                        JsonObject jsonWaypoint = waypointEntry.getValue().getAsJsonObject();
                        double x = jsonWaypoint.get("x").getAsDouble();
                        double y = jsonWaypoint.get("y").getAsDouble();
                        double z = jsonWaypoint.get("z").getAsDouble();
                        boolean stopVelocity = jsonWaypoint.get("stopVelocity").getAsBoolean();
                        boolean useJump = jsonWaypoint.get("useJump").getAsBoolean();
                        boolean look = jsonWaypoint.get("look").getAsBoolean();
                        boolean click = jsonWaypoint.get("click").getAsBoolean();
                        float yaw = jsonWaypoint.get("yaw").getAsFloat();
                        float pitch = jsonWaypoint.get("pitch").getAsFloat();
                        boolean independent = jsonWaypoint.get("independent").getAsBoolean();
                        String routeItem = jsonWaypoint.get("routeItem").getAsString();
                        boolean edgeJump = jsonWaypoint.get("edgeJump").getAsBoolean();
                        boolean bonzo;
                        try{
                            bonzo = jsonWaypoint.get("bonzo").getAsBoolean();
                        }catch (Exception e){
                            bonzo = false;
                        }
                        boolean wait = jsonWaypoint.get("wait").getAsBoolean();
                        float time = jsonWaypoint.get("time").getAsFloat();
                        route.addWaypoints(x, y, z, stopVelocity, useJump, look, click, yaw, pitch, independent, RouteItem.valueOf(routeItem), edgeJump, bonzo, wait, time);
                    }

                    userRoute.routes.add(route);
                    //DelClient.settingsManager.rSetting(new Setting(route.getName(), userRoute, false, "user_route_routes:"+route.getName()));
                }
                //userRoute.moduleButtonUser.updateSettings();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

