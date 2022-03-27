package com.seepine.auth.annotation;

import java.lang.annotation.*;

/**
 * 为类中所有@Permission加上前缀，作用于类上
 *
 * @author seepine
 * @since 2.0.0
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionPrefix {
  String value() default "";
}
