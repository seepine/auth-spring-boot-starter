package com.seepine.auth.annotation;

import java.lang.annotation.*;

/**
 * 直接暴露接口，不鉴权
 *
 * @author seepine
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Expose {}
