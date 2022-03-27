package com.seepine.auth.annotation;

import java.lang.annotation.*;

/**
 * 不暴露接口，需要鉴权 使用场景：某controller上加了@Expose，但是内部某个方法需要鉴权
 *
 * @author seepine
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotExpose {}
