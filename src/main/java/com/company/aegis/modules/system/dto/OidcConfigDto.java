package com.company.aegis.modules.system.dto;

import lombok.Data;
import java.util.Set;

@Data
public class OidcConfigDto {
    private Boolean enabled;
    private String clientId;
    private String clientSecret;
    private Set<String> redirectUris;
    private Set<String> grantTypes;
    private Set<String> scopes;
    private Long accessTokenTimeToLive; // Seconds
}
