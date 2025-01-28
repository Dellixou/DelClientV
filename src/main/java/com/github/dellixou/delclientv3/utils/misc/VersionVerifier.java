package com.github.dellixou.delclientv3.utils.misc;

import com.github.dellixou.delclientv3.DelClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class VersionVerifier {

    private static final String VERSION_URL = "https://raw.githubusercontent.com/Dellixou/DelClientV/master/data/version.txt?token=GHSAT0AAAAAACPBMAH3GEZXSQIYYKIZMKMCZVEZW7Q";
    private static List<String> versionString;

    public static void getCurrentVersion() throws IOException {
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

            // Get Version
            List<String> version = new ArrayList<>();
            URL url = new URL(VERSION_URL);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    version.add(line.trim());
                }
            }
            versionString = version;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Restore the original SSLSocketFactory and HostnameVerifier
            HttpsURLConnection.setDefaultSSLSocketFactory(originalSSLSocketFactory);
            HttpsURLConnection.setDefaultHostnameVerifier(originalHostnameVerifier);
        }
    }

    public static String getModVersion() {
        try {

            InputStream inputStream = DelClient.class.getResourceAsStream("/mcmod.info");

            if (inputStream == null) {
                return "?";
            }

            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            JsonArray modList = gson.fromJson(reader, JsonArray.class);

            reader.close();

            if (modList != null && modList.size() > 0) {
                JsonObject modInfo = modList.get(0).getAsJsonObject();

                if (modInfo.has("version")) {
                    return modInfo.get("version").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "?";
    }

    public static boolean isLastVersion(){
        String modVersion = getModVersion();
        System.out.println(modVersion);
        System.out.println(versionString);
        return versionString != null && versionString.contains(modVersion);
    }
}
