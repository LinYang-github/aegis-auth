package com.company.aegis.modules.system.service.impl;

import com.company.aegis.modules.system.dto.OidcConfigDto;
import com.company.aegis.modules.system.entity.SysApplication;
import com.company.aegis.modules.system.service.OidcClientService;
import com.company.aegis.modules.system.service.SysApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OidcClientServiceImpl implements OidcClientService {

    private final SysApplicationService sysApplicationService;
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OidcConfigDto getOidcConfig(Long appId) {
        SysApplication app = sysApplicationService.getById(appId);
        if (app == null)
            throw new RuntimeException("App not found");

        RegisteredClient client = registeredClientRepository.findByClientId(app.getCode());
        OidcConfigDto dto = new OidcConfigDto();
        dto.setClientId(app.getCode());

        if (client != null) {
            dto.setEnabled(true);
            dto.setRedirectUris(client.getRedirectUris());
            dto.setGrantTypes(client.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue)
                    .collect(Collectors.toSet()));
            dto.setScopes(client.getScopes());
            dto.setAccessTokenTimeToLive(client.getTokenSettings().getAccessTokenTimeToLive().getSeconds());
            // Secret is hashed, cannot return original. Frontend should only show generated
            // one once or verify hash.
            // For now, we denote existence.
            dto.setClientSecret("******");
        } else {
            dto.setEnabled(false);
        }
        return dto;
    }

    @Override
    @Transactional
    public void updateOidcConfig(Long appId, OidcConfigDto config) {
        SysApplication app = sysApplicationService.getById(appId);
        if (app == null)
            throw new RuntimeException("App not found");

        if (Boolean.TRUE.equals(config.getEnabled())) {
            // Create or Update
            RegisteredClient existing = registeredClientRepository.findByClientId(app.getCode());

            RegisteredClient.Builder builder;
            if (existing != null) {
                builder = RegisteredClient.from(existing);
            } else {
                builder = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(app.getCode())
                        .clientName(app.getName())
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);

                // Initial secret generation if new
                String secret = UUID.randomUUID().toString().replace("-", "");
                builder.clientSecret(passwordEncoder.encode(secret));
                // Note: We can't return the secret here unless we rotate it.
                // Logic: If enabling first time, maybe we should return secret or user must
                // click Rotate.
            }

            if (config.getRedirectUris() != null) {
                builder.redirectUris(uris -> {
                    uris.clear();
                    uris.addAll(config.getRedirectUris());
                });
            }

            if (config.getGrantTypes() != null) {
                builder.authorizationGrantTypes(types -> {
                    types.clear();
                    config.getGrantTypes().forEach(t -> types.add(new AuthorizationGrantType(t)));
                    // Always ensure some defaults if empty?
                });
            } else {
                // Defaults
                builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);
            }

            if (config.getScopes() != null && !config.getScopes().isEmpty()) {
                builder.scopes(scopes -> {
                    scopes.clear();
                    scopes.addAll(config.getScopes());
                });
            } else {
                builder.scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE);
            }

            // Settings
            builder.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build()); // Force consent
                                                                                                        // for now
            builder.tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(30)).build());

            registeredClientRepository.save(builder.build());
        } else {
            // Disable = Delete?? Or just mark disabled? Spring Authorization Server doesn't
            // have "disable" flag easily.
            // Usually we might just delete it from repo logic or set invalid scopes/secret.
            // For simplicity, let's NO-OP or Delete.
            RegisteredClient existing = registeredClientRepository.findByClientId(app.getCode());
            if (existing != null) {
                // Since JdbcRegisteredClientRepository doesn't strictly have delete method
                // exposed in interface without casting?
                // Wait, RegisteredClientRepository does NOT have delete.
                // We might need to cast to JdbcRegisteredClientRepository or use custom logic.
                // Given standard interface, we can't easily delete.
                // We can set clientSecretExpiresAt to path if we want to disable.
                RegisteredClient disabled = RegisteredClient.from(existing)
                        .clientSecretExpiresAt(java.time.Instant.now())
                        .build();
                registeredClientRepository.save(disabled);
            }
        }
    }

    @Override
    public String rotateSecret(Long appId) {
        SysApplication app = sysApplicationService.getById(appId);
        if (app == null)
            throw new RuntimeException("App not found");

        RegisteredClient existing = registeredClientRepository.findByClientId(app.getCode());
        if (existing == null)
            throw new RuntimeException("OIDC not enabled for this app");

        String newSecret = UUID.randomUUID().toString().replace("-", "");
        RegisteredClient updated = RegisteredClient.from(existing)
                .clientSecret(passwordEncoder.encode(newSecret))
                .clientSecretExpiresAt(null) // Reset expiry
                .build();
        registeredClientRepository.save(updated);

        return newSecret;
    }
}
