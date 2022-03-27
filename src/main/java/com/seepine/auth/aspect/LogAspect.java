package com.seepine.auth.aspect;

import com.seepine.auth.annotation.Log;
import com.seepine.auth.entity.LogEvent;
import com.seepine.auth.service.AuthLogService;
import com.seepine.auth.util.CurrentTimeMillisClock;
import com.seepine.auth.util.LogUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

  @Autowired(required = false)
  private AuthLogService authLogService;

  /** 频率限制切入点(注解类的路径) */
  @Pointcut(value = "@annotation(com.seepine.auth.annotation.Log)")
  public void logPointCut() {}

  /**
   * 切面请求频率限制
   *
   * @param joinPoint joinPoint
   */
  @Around("logPointCut()")
  public Object doAfter(ProceedingJoinPoint joinPoint) throws Throwable {
    Signature signature = joinPoint.getSignature();
    MethodSignature methodSignature = (MethodSignature) signature;
    Object result = null;
    Throwable exception = null;
    long startTime = CurrentTimeMillisClock.now();
    try {
      result = joinPoint.proceed();
    } catch (Throwable e) {
      exception = e;
    }
    if (authLogService != null) {
      Log log = methodSignature.getMethod().getAnnotation(Log.class);
      LogEvent logEvent = LogUtil.gen(log, CurrentTimeMillisClock.now() - startTime, exception);
      authLogService.save(logEvent);
    }
    if (exception != null) {
      throw exception;
    }
    return result;
  }
}
