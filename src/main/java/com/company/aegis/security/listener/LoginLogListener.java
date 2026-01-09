package com.company.aegis.security.listener;

import com.company.aegis.modules.system.entity.SysLog;
import com.company.aegis.modules.system.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginLogListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final SysLogService sysLogService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();

        // Get Request
        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
        } catch (Exception e) {
            // Ignore
        }

        String ip = "Unknown";
        if (request != null) {
            ip = getClientIP(request);
        }

        sysLogService.saveLog(username, "Login", "AuthenticationSuccessEvent", "", ip, 0L);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
