package com.github.dellixou.delclientv3.utils.misc;

import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

public class RouteLoader{

    private static final String GITHUB_URL = "https://raw.githubusercontent.com/Dellixou/whynot/main/routes/";

    public void loadRoutes() {
        try {
            UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");
            userRoute.routes.clear();

            // Fetch all JSON files from GitHub
            String[] routeFiles = {
                    "1.4.3.json",
                    "2.dev.ee3.json",
                    "3.2.dev.json",
                    "4.3.levers.json"
            };

            for (String routeFileName : routeFiles) {
                JsonObject jsonRoute = fetchJsonFromGitHub(routeFileName);

                if (jsonRoute != null) {
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
                        boolean bonzo = jsonWaypoint.has("bonzo") ? jsonWaypoint.get("bonzo").getAsBoolean() : false;
                        boolean wait = jsonWaypoint.get("wait").getAsBoolean();
                        float time = jsonWaypoint.get("time").getAsFloat();
                        route.addWaypoints(x, y, z, stopVelocity, useJump, look, click, yaw, pitch, independent, RouteItem.valueOf(routeItem), edgeJump, bonzo, wait, time);
                    }

                    userRoute.routes.add(route);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JsonObject fetchJsonFromGitHub(String fileName) throws IOException {
        // Save the original SSLSocketFactory and HostnameVerifier
        SSLSocketFactory originalSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        HostnameVerifier originalHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

        // Disable SSL certificate verification
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        }catch (Exception e){
            e.printStackTrace();
        }
        // Construct GitHub URL for the specific JSON file
        URL url = new URL(GITHUB_URL + fileName);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            // Use Gson to parse JSON
            JsonParser jsonParser = new JsonParser();
            return jsonParser.parse(in).getAsJsonObject();
        } finally {
            connection.disconnect();
            HttpsURLConnection.setDefaultSSLSocketFactory(originalSSLSocketFactory);
            HttpsURLConnection.setDefaultHostnameVerifier(originalHostnameVerifier);
        }
    }

    public void unloadRoute() {
        UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");
        if(userRoute != null)
        userRoute.routes.clear();
    }
}
