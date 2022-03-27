package com.seepine.auth.interceptor;

import com.seepine.auth.annotation.NotSecret;
import com.seepine.auth.annotation.Secret;
import com.seepine.auth.entity.AuthProperties;
import com.seepine.auth.enums.AuthExceptionType;
import com.seepine.auth.exception.AuthException;
import com.seepine.auth.service.AuthSecretService;
import com.seepine.auth.util.AnnotationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SecretInterceptor
 *
 * @author seepine
 * @since 1.3.0
 */
@Slf4j
public class SecretInterceptor implements HandlerInterceptor {

  private final AuthProperties authProperties;
  private final AuthSecretService secretService;

  public SecretInterceptor(AuthProperties authProperties, AuthSecretService secretService) {
    this.authProperties = authProperties;
    this.secretService = secretService;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      Object handler) {
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }
    // has @NotSecret
    if (AnnotationUtil.hasAnnotation(handler, NotSecret.class)) {
      return true;
    }
    // not @Secret and not defaultAllSecret
    if (!AnnotationUtil.hasAnnotation(handler, Secret.class)
        && !authProperties.isDefaultAllSecret()) {
      return true;
    }
    String timeSecret = httpServletRequest.getHeader(authProperties.getSecretHeader());
    if (timeSecret == null || "".equals(timeSecret)) {
      throw new AuthException(AuthExceptionType.NOT_SECRET);
    }
    if (secretService.verify(timeSecret)) {
      return true;
    }
    throw new AuthException(AuthExceptionType.INVALID_SECRET);
  }
}
