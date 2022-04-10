package com.seepine.auth.entity;

import lombok.Getter;
import lombok.ToString;
import org.springframework.core.Ordered;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * auth参数类
 *
 * @author seepine
 */
@Getter
@ToString
public class AuthProperties {
  /** 请求头参数 */
  String header = "token";
  /** 安全参数相关 */
  String secretHeader = "secret";
  /** 缓存前缀 */
  String cacheKey = "com.seepine.auth";
  /** token过期时间,默认3天 */
  Duration timeout = Duration.ofDays(3);
  /** 是否自动重置token过期时间 */
  Boolean resetTimeout = false;
  /** secret允许超时毫秒数，默认10分钟 */
  long secretTimeout = 10 * 60 * 1000;
  /** 拦截器order值,默认为最高级 */
  int interceptorOrder = Ordered.HIGHEST_PRECEDENCE;

  boolean enableSecret = false;
  boolean enableAuth = true;
  boolean defaultAllSecret = false;

  List<String> excludePath = new ArrayList<>();
  List<String> excludeSecretPath = new ArrayList<>();
  String rsaPrivateKey;
  String rsaOldPrivateKey;

  public AuthProperties rsaPrivateKey(String rsaPrivateKey) {
    this.rsaPrivateKey = rsaPrivateKey;
    return this;
  }

  public AuthProperties rsaPrivateKey(String rsaPrivateKey, String rsaOldPrivateKey) {
    this.rsaPrivateKey = rsaPrivateKey;
    this.rsaOldPrivateKey = rsaOldPrivateKey;
    return this;
  }

  public AuthProperties interceptorOrder(int interceptorOrder) {
    this.interceptorOrder = interceptorOrder;
    return this;
  }

  public AuthProperties enableSecret(boolean enableSecret) {
    this.enableSecret = enableSecret;
    return this;
  }

  public AuthProperties secretTimeout(long secretTimeout) {
    this.secretTimeout = secretTimeout;
    return this;
  }

  public AuthProperties enableAuth(boolean enableAuth) {
    this.enableAuth = enableAuth;
    return this;
  }

  public AuthProperties defaultAllSecret(boolean defaultAllSecret) {
    this.defaultAllSecret = defaultAllSecret;
    return this;
  }

  public AuthProperties excludePath(String... excludePath) {
    this.excludePath.addAll(Arrays.asList(excludePath));
    return this;
  }

  public AuthProperties excludeSecretPath(String... excludePath) {
    this.excludeSecretPath.addAll(Arrays.asList(excludePath));
    return this;
  }

  public AuthProperties secretHeader(String secretHeader) {
    this.secretHeader = secretHeader;
    return this;
  }

  public AuthProperties header(String header) {
    this.header = header;
    return this;
  }

  public AuthProperties cacheKey(String cacheKey) {
    this.cacheKey = cacheKey;
    return this;
  }

  public AuthProperties timeout(Duration duration) {
    this.timeout = duration;
    return this;
  }

  public AuthProperties timeout(Duration duration, Boolean resetTimeout) {
    this.timeout = duration;
    this.resetTimeout = resetTimeout;
    return this;
  }

  public AuthProperties resetTimeout(Boolean resetTimeout) {
    this.resetTimeout = resetTimeout;
    return this;
  }
}
