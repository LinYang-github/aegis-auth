package com.company.aegis.common.controller;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DebugController {

    private final RegisteredClientRepository registeredClientRepository;

    @GetMapping("/debug/client/{clientId}")
    public String getClient(@PathVariable String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            return "Client not found: " + clientId;
        }
        String grants = client.getAuthorizationGrantTypes().stream()
                .map(g -> g.getValue())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        return "Client found: " + client.getId() + ", Method: " + client.getClientAuthenticationMethods() +
                ", Grants: [" + grants + "]" +
                ", Redirects: " + client.getRedirectUris() + ", Settings: " + client.getClientSettings().getSettings() +
                ", Secret: " + client.getClientSecret();
    }
}
