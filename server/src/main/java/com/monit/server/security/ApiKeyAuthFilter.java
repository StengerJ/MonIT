package com.monit.server.security;

import com.monit.server.entity.ClientEntity;
import com.monit.server.repository.ClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ClientRepository clientRepository;

    public ApiKeyAuthFilter(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-Key");
        Optional<ClientEntity> client = apiKey == null ? Optional.empty() : clientRepository.findByApiKey(apiKey);

        if (client.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        request.setAttribute("clientId", client.get().getId());
        filterChain.doFilter(request, response);
    }
}
