package com.github.dellixou.delclientv3.utils.misc;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class UUIDVerifier {

    private static final String ALLOWED_UUID_URL = "https://raw.githubusercontent.com/Dellixou/whynot/main/uuids_users.txt";
    private List<String> allowedUUIDs;

    public List<String> fetchAllowedUUIDs(EntityPlayer player) throws IOException {
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

            // Fetch allowed UUIDs
            List<String> uuids = new ArrayList<>();
            URL url = new URL(ALLOWED_UUID_URL);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    uuids.add(line.trim());
                }
            }
            allowedUUIDs = uuids;
            return uuids;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            // Restore the original SSLSocketFactory and HostnameVerifier
            HttpsURLConnection.setDefaultSSLSocketFactory(originalSSLSocketFactory);
            HttpsURLConnection.setDefaultHostnameVerifier(originalHostnameVerifier);

            if(allowedUUIDs.contains(player.getUniqueID().toString())){
                DelClient.sendChatToClient("&7Authorization : &aAuthorized!");
                DelClient.instance.setIsAuthorized(true);
            }else{
                DelClient.sendChatToClient("&7Authorization : &cDenied!");
                DelClient.instance.setIsAuthorized(false);
                Minecraft.getMinecraft().crashed(new CrashReport("Access denied to DelClient", null));
                try{
                    Thread.sleep(10);
                }catch (Exception ignored) { }
                Minecraft.getMinecraft().shutdownMinecraftApplet();
            }

        }
    }

    public boolean isUUIDAllowed(String uuid) {
        return allowedUUIDs != null && allowedUUIDs.contains(uuid);
    }
}
