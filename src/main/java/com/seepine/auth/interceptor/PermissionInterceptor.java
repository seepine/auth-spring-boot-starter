package com.seepine.auth.interceptor;

import com.seepine.auth.annotation.Permission;
import com.seepine.auth.annotation.PermissionPrefix;
import com.seepine.auth.util.PermissionUtil;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * PermissionInterceptor
 *
 * @author seepine
 * @since 2.0.0
 */
public class PermissionInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      Object handler) {
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }
    HandlerMethod handlerMethod = (HandlerMethod) handler;
    Method method = handlerMethod.getMethod();
    Permission permission = null;
    if (method.isAnnotationPresent(Permission.class)) {
      permission = method.getAnnotation(Permission.class);
    } else {
      try {
        // 1.判断是否重写父类方法
        Method parentMethod =
            method.getDeclaringClass().getSuperclass().getDeclaredMethod(method.getName());
        // 2.是的话判断父类的方法是否有注解
        if (parentMethod.isAnnotationPresent(Permission.class)) {
          permission = parentMethod.getAnnotation(Permission.class);
        }
      } catch (NoSuchMethodException ignored) {
      }
    }
    if (permission != null) {
      PermissionPrefix prefix = handlerMethod.getBeanType().getAnnotation(PermissionPrefix.class);
      PermissionUtil.verify(
          permission,
          prefix == null
              ? method.getDeclaringClass().getAnnotation(PermissionPrefix.class)
              : prefix);
    }
    return true;
  }
}
