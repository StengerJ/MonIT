package com.monit.server.security;

import org.apache.catalina.connector.Connector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

class MtlsConnectorConfigTest {

    @Test
    void doesNotAddConnectorWhenDisabled() {
        MtlsConnectorConfig config = new MtlsConnectorConfig(false, 8443, "", "", "", "");
        TomcatServletWebServerFactory factory = mock(TomcatServletWebServerFactory.class);

        config.customize(factory);

        verify(factory, never()).addAdditionalTomcatConnectors(any(Connector.class));
    }

    @Test
    void addsAdditionalConnectorWhenEnabled() {
        MtlsConnectorConfig config = new MtlsConnectorConfig(
                true, 8443, "keystore.p12", "changeit", "truststore.p12", "changeit");
        TomcatServletWebServerFactory factory = mock(TomcatServletWebServerFactory.class);

        config.customize(factory);

        verify(factory).addAdditionalTomcatConnectors(any(Connector.class));
    }
}
