package com.company.aegis.common.aspect;

import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.company.aegis.common.annotation.AutoLog;
import com.company.aegis.modules.system.entity.SysLog;
import com.company.aegis.modules.system.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.security.Principal;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogService sysLogService;

    @Pointcut("@annotation(com.company.aegis.common.annotation.AutoLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        // Execute method
        Object result = point.proceed();
        // Execution time
        long time = System.currentTimeMillis() - beginTime;

        // Save Log asynchronously could be better, but synchronous is safer for now.
        saveSysLog(point, time);

        return result;
    }

    private void saveSysLog(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        AutoLog syslog = method.getAnnotation(AutoLog.class);
        String operation = (syslog != null) ? syslog.value() : "";

        // Get Request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        // Method Name
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        String methodStr = className + "." + methodName + "()";

        // Params
        String params = "";
        Object[] args = joinPoint.getArgs();
        try {
            // Simple JSON serialization, beware of circular references or large objects
            // Filtering out common non-serializable objects like HttpServletRequest
            Object[] validArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof jakarta.servlet.ServletRequest
                        || args[i] instanceof jakarta.servlet.ServletResponse) {
                    continue;
                }
                validArgs[i] = args[i];
            }
            params = JSONUtil.toJsonStr(validArgs);
        } catch (Exception e) {
            // Ignore param serialization errors
        }

        // IP
        String ip = getClientIP(request);

        // Username
        String username = "";
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            username = principal.getName();
        }

        // Save
        sysLogService.saveLog(username, operation, methodStr, params, ip, time);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
