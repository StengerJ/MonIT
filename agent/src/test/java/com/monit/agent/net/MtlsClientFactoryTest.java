package com.monit.agent.net;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MtlsClientFactoryTest {

    @Test
    void disabledReturnsPlainRestTemplate() {
        RestTemplate restTemplate = MtlsClientFactory.build(false, "", "", "", "");

        assertThat(restTemplate.getRequestFactory()).isInstanceOf(SimpleClientHttpRequestFactory.class);
    }

    @Test
    void enabledWithMissingKeystoreFailsFast() {
        assertThatThrownBy(() -> MtlsClientFactory.build(
                true, "does-not-exist.p12", "changeit", "does-not-exist.p12", "changeit"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to configure mTLS client");
    }
}
