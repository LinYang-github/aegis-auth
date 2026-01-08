package com.company.aegis.modules.system.service;

import com.company.aegis.modules.system.dto.OidcConfigDto;

public interface OidcClientService {
    OidcConfigDto getOidcConfig(Long appId);

    void updateOidcConfig(Long appId, OidcConfigDto config);

    String rotateSecret(Long appId);
}
