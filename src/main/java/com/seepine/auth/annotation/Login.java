package com.seepine.auth.annotation;

import java.lang.annotation.*;

/**
 * 登录接口
 *
 * @author seepine
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Login {}
