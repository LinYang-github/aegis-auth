package com.company.aegis.common.annotation;

import java.lang.annotation.*;

/**
 * Annotation for automatic logging of method execution
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {
    /**
     * Operation Description
     */
    String value() default "";
}
