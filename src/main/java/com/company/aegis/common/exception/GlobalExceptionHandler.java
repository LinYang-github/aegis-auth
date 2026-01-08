package com.company.aegis.common.exception;

import com.company.aegis.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("System error", e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public Result<String> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException e) {
        // Return 404 code
        return Result.fail(404, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("Runtime error", e);
        return Result.fail(e.getMessage());
    }
}
