package com.seepine.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
  /** 是否开启接口加密 */
  Boolean enabled = Boolean.TRUE;
  /** 请求头参数 */
  String header = "token";
  /** 缓存前缀 */
  String cachePrefix = "com.seepine.auth";
  /** 是否自动重置登录过期时间 */
  Boolean resetTimeout = Boolean.TRUE;
  /** 过期时间，默认3天 */
  Long timeout = 3 * 24 * 60 * 60L;
  /** 拦截器排除的pathPatterns */
  String[] excludePathPatterns = new String[] {};
  /** 拦截器的order */
  Integer interceptorOrder = Ordered.HIGHEST_PRECEDENCE + 100;
}
