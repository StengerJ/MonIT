package com.monit.agent.identity;

import com.monit.common.RegisterRequest;
import com.monit.common.RegisterResponse;
import org.springframework.web.client.RestTemplate;

public class RegistrationClient {

    private final RestTemplate restTemplate;

    public RegistrationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RegisterResponse register(String serverBaseUrl, String hostname, String bootstrapSecret) {
        RegisterRequest request = new RegisterRequest(hostname, bootstrapSecret);
        return restTemplate.postForObject(serverBaseUrl + "/api/register", request, RegisterResponse.class);
    }
}
