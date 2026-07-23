package com.monit.agent.net;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

/**
 * Builds the RestTemplate the agent uses to talk to the server. When mTLS is enabled,
 * the client presents a certificate from its keystore and validates the server against
 * its truststore; this runs alongside the API key, not instead of it.
 */
public final class MtlsClientFactory {

    private MtlsClientFactory() {
    }

    public static RestTemplate build(boolean enabled,
                                      String keystorePath,
                                      String keystorePassword,
                                      String truststorePath,
                                      String truststorePassword) {
        if (!enabled) {
            return new RestTemplate();
        }
        try {
            SSLContext sslContext = buildSslContext(keystorePath, keystorePassword, truststorePath, truststorePassword);
            HttpClient httpClient = HttpClient.newBuilder().sslContext(sslContext).build();
            return new RestTemplate(new JdkClientHttpRequestFactory(httpClient));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure mTLS client", e);
        }
    }

    private static SSLContext buildSslContext(String keystorePath,
                                               String keystorePassword,
                                               String truststorePath,
                                               String truststorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(Path.of(keystorePath))) {
            keyStore.load(in, keystorePassword.toCharArray());
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(Path.of(truststorePath))) {
            trustStore.load(in, truststorePassword.toCharArray());
        }
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }
}
