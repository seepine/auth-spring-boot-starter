package com.seepine.auth.aspect;

import com.seepine.auth.annotation.RateLimit;
import com.seepine.auth.enums.AuthExceptionType;
import com.seepine.auth.exception.AuthException;
import com.seepine.auth.util.RateLimitUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @author seepine
 */
@Aspect
@Component
public class RateLimitAspect {

  /** 频率限制切入点(注解类的路径) */
  @Pointcut(value = "@annotation(com.seepine.auth.annotation.RateLimit)")
  public void rateLimitPointCut() {}

  /**
   * 切面请求频率限制
   *
   * @param joinPoint joinPoint
   */
  @Before("rateLimitPointCut()")
  public void doBefore(JoinPoint joinPoint) {
    Signature signature = joinPoint.getSignature();
    MethodSignature methodSignature = (MethodSignature) signature;
    RateLimit reqLimit = methodSignature.getMethod().getAnnotation(RateLimit.class);
    if (!RateLimitUtil.verify(
        RateLimitUtil.getUniqueId(reqLimit.global()),
        reqLimit.second() < 0 ? reqLimit.value() : reqLimit.second(),
        reqLimit.minute(),
        reqLimit.hour(),
        reqLimit.day())) {
      throw new AuthException(AuthExceptionType.RATE_LIMIT);
    }
  }
}
