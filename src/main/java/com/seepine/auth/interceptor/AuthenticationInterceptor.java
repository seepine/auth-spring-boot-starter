package com.seepine.auth.interceptor;

import com.seepine.auth.annotation.Expose;
import com.seepine.auth.annotation.Login;
import com.seepine.auth.annotation.NotExpose;
import com.seepine.auth.entity.AuthProperties;
import com.seepine.auth.enums.AuthExceptionType;
import com.seepine.auth.exception.AuthException;
import com.seepine.auth.util.AnnotationUtil;
import com.seepine.auth.util.AuthUtil;
import com.seepine.auth.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author seepine
 */
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {
  AuthProperties authProperties;

  public AuthenticationInterceptor(AuthProperties authProperties) {
    this.authProperties = authProperties;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      Object handler) {
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }
    // 登录接口，直接放行
    if (AnnotationUtil.hasAnnotation(handler, Login.class)) {
      return true;
    }
    String token = httpServletRequest.getHeader(authProperties.getHeader());
    boolean isFindAndFill = AuthUtil.findAndFill(token);
    // not @NotExpose and has @Expose, pass
    if (!AnnotationUtil.hasAnnotation(handler, NotExpose.class)
        && AnnotationUtil.hasAnnotation(handler, Expose.class)) {
      return true;
    }
    // token is blank, Not Acceptable
    if (StrUtil.isBlank(token)) {
      throw new AuthException(AuthExceptionType.NOT_TOKEN);
    }
    // has token but can not get user info, Unauthorized
    if (!isFindAndFill) {
      throw new AuthException(AuthExceptionType.INVALID_TOKEN);
    }
    return true;
  }

  /**
   * clear ThreadLocal
   *
   * @param httpServletRequest httpServletRequest
   * @param httpServletResponse httpServletResponse
   * @param o o
   * @param e e
   */
  @Override
  public void afterCompletion(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      Object o,
      Exception e) {
    AuthUtil.clear();
  }
}
