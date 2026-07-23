package com.monit.server.security;

import com.monit.server.entity.ClientEntity;
import com.monit.server.repository.ClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiKeyAuthFilterTest {

    @Test
    void allowsRequestWithValidApiKey() throws Exception {
        ClientRepository repository = mock(ClientRepository.class);
        ClientEntity client = new ClientEntity();
        UUID clientId = UUID.randomUUID();
        client.setId(clientId);
        client.setApiKey("valid-key");
        when(repository.findByApiKey("valid-key")).thenReturn(Optional.of(client));

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(repository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "valid-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        assertThat(request.getAttribute("clientId")).isEqualTo(clientId);
    }

    @Test
    void rejectsRequestWithMissingOrInvalidApiKey() throws Exception {
        ClientRepository repository = mock(ClientRepository.class);
        when(repository.findByApiKey("bad-key")).thenReturn(Optional.empty());

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(repository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "bad-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
    }
}
