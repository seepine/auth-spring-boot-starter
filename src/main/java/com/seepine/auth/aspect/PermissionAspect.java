package com.seepine.auth.aspect;

import com.seepine.auth.annotation.Permission;
import com.seepine.auth.annotation.PermissionPrefix;
import com.seepine.auth.util.PermissionUtil;
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
public class PermissionAspect {
  /** 频率限制切入点(注解类的路径) */
  @Pointcut(value = "@annotation(com.seepine.auth.annotation.Permission)")
  public void permissionPointCut() {}

  /**
   * 切面请求频率限制
   *
   * @param joinPoint joinPoint
   */
  @Before("permissionPointCut()")
  public void doBefore(JoinPoint joinPoint) {
    Signature signature = joinPoint.getSignature();
    MethodSignature methodSignature = (MethodSignature) signature;
    Permission permission = methodSignature.getMethod().getAnnotation(Permission.class);
    PermissionPrefix permissionPrefix =
        joinPoint.getTarget().getClass().getAnnotation(PermissionPrefix.class);
    PermissionUtil.verify(permission, permissionPrefix);
  }
}
