package com.seepine.auth.annotation;

import java.lang.annotation.*;

/**
 * 速率限制
 *
 * @author seepine
 * @since 2.0.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
  /**
   * second的别名，值将会赋给second
   *
   * @return num
   */
  int value() default 20;
  /**
   * 一秒速率，默认一秒20次，优先级比value高，为负数时将会取value的值
   *
   * <p>例如@RateLimit(4) ，second会是4
   *
   * <p>例如@RateLimit(value=4,second=10) ，second会是10
   *
   * <p>例如@RateLimit(second=30) ，second会是30
   *
   * @return num
   */
  int second() default -1;
  /**
   * 一分钟分速率
   *
   * @return num
   */
  int minute() default 0;
  /**
   * 一小时速率
   *
   * @return num
   */
  int hour() default 0;
  /**
   * 一天速率
   *
   * @return num
   */
  int day() default 0;

  /**
   * 是否全局共享，true的话则所有人的该接口请求共用计数
   *
   * @return bool
   */
  boolean global() default false;
}
