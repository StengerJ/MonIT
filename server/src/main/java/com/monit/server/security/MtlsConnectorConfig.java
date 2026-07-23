package com.monit.server.security;

import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Adds a second Tomcat connector that requires a client certificate, alongside the
 * plain HTTP connector the dashboard and API key auth use. Agents that present a
 * trusted client cert can reach the same endpoints (e.g. /api/reports) over this
 * port instead of (or in addition to) the API key.
 */
@Configuration
@Profile("!test")
public class MtlsConnectorConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final boolean enabled;
    private final int port;
    private final String keystorePath;
    private final String keystorePassword;
    private final String truststorePath;
    private final String truststorePassword;

    public MtlsConnectorConfig(@Value("${monit.server.mtls.enabled:false}") boolean enabled,
                                @Value("${monit.server.mtls.port:8443}") int port,
                                @Value("${monit.server.mtls.keystore:}") String keystorePath,
                                @Value("${monit.server.mtls.keystore-password:}") String keystorePassword,
                                @Value("${monit.server.mtls.truststore:}") String truststorePath,
                                @Value("${monit.server.mtls.truststore-password:}") String truststorePassword) {
        this.enabled = enabled;
        this.port = port;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.truststorePath = truststorePath;
        this.truststorePassword = truststorePassword;
    }

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        if (!enabled) {
            return;
        }
        factory.addAdditionalTomcatConnectors(buildConnector());
    }

    private Connector buildConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(port);
        connector.setScheme("https");
        connector.setSecure(true);
        connector.setProperty("SSLEnabled", "true");

        SSLHostConfig sslHostConfig = new SSLHostConfig();
        SSLHostConfigCertificate certificate =
                new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA);
        certificate.setCertificateKeystoreFile(keystorePath);
        certificate.setCertificateKeystorePassword(keystorePassword);
        sslHostConfig.addCertificate(certificate);
        sslHostConfig.setTruststoreFile(truststorePath);
        sslHostConfig.setTruststorePassword(truststorePassword);
        sslHostConfig.setCertificateVerification("required");
        connector.addSslHostConfig(sslHostConfig);

        return connector;
    }
}
