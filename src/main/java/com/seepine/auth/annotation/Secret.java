package com.seepine.auth.annotation;

import java.lang.annotation.*;

/**
 * 是否需要密钥
 *
 * @author seepine
 * @since 1.3.0
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Secret {}
