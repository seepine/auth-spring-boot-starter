package com.seepine.auth.annotation;

import java.lang.annotation.*;

/**
 * 日志
 *
 * @author seepine
 * @since 2.0.0
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
  /***
   * title别名
   * @return 日志标题
   */
  String value() default "";

  /**
   * 日志标题，优先级高于value
   *
   * @return 例如：添加用户
   */
  String title() default "";

  /**
   * 日志内容
   *
   * @return 对日志进行详细描述
   */
  String content() default "";
  /**
   * 模块
   *
   * @return 例如订单模块order、支付模块pay
   */
  String module() default "";
}
