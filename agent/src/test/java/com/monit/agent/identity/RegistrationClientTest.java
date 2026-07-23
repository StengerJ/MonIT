package com.monit.agent.identity;

import com.monit.common.RegisterResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RegistrationClientTest {

    @Test
    void registersAndParsesResponse() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        server.expect(requestTo("http://server:8081/api/register"))
                .andExpect(method(POST))
                .andExpect(jsonPath("$.hostname").value("host-a"))
                .andExpect(jsonPath("$.bootstrapSecret").value("shared-secret"))
                .andRespond(withSuccess(
                        "{\"clientId\":\"client-1\",\"apiKey\":\"api-key-123\"}",
                        MediaType.APPLICATION_JSON));

        RegistrationClient client = new RegistrationClient(restTemplate);
        RegisterResponse response = client.register("http://server:8081", "host-a", "shared-secret");

        assertThat(response.getClientId()).isEqualTo("client-1");
        assertThat(response.getApiKey()).isEqualTo("api-key-123");
        server.verify();
    }
}
