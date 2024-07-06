package com.github.dellixou.delclientv3.utils.misc;

import com.github.dellixou.delclientv3.utils.Reference;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class VersionVerifier {

    private static final String VERSION_URL = "https://raw.githubusercontent.com/Dellixou/whynot/main/version.txt";
    private static List<String> versionString;

    public static List<String> getCurrentVersion() throws IOException {
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
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            // Restore the original SSLSocketFactory and HostnameVerifier
            HttpsURLConnection.setDefaultSSLSocketFactory(originalSSLSocketFactory);
            HttpsURLConnection.setDefaultHostnameVerifier(originalHostnameVerifier);
        }
    }

    public static boolean isLastVersion(String version) {
        return versionString != null && versionString.contains(Reference.VERSION);
    }
}
