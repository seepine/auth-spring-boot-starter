package com.seepine.auth.annotation;

import java.lang.annotation.*;

/**
 * 鉴权，作用于方法上
 *
 * @author seepine
 * @since 2.0.0
 */
@Documented
@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
  /** 必须包含所有权限才能通过 */
  String[] value() default {};

  /** 满足任意一个权限即可通过 */
  String[] or() default {};

  /** 若类上有@PermissionPrefix时，是否要拼接前缀，默认要 */
  boolean prefix() default true;
}
