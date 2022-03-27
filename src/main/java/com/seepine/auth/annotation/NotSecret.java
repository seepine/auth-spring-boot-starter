package com.seepine.auth.annotation;

import java.lang.annotation.*;

/**
 * 不需要密钥 使用场景：某controller上加了@Secret，但是内部某个方法不需要
 *
 * @author seepine
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotSecret {}
