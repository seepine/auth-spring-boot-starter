package com.seepine.auth.util;

import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author seepine
 */
public class AnnotationUtil {

  /**
   * 方法上有注解或类上有注解
   *
   * @param handler handler
   * @param annotationClass annotationClass
   * @return boolean
   */
  public static boolean hasAnnotation(Object handler, Class<? extends Annotation> annotationClass) {
    try {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      Method method = handlerMethod.getMethod();
      return method.isAnnotationPresent(annotationClass)
          || method.getDeclaringClass().isAnnotationPresent(annotationClass);
    } catch (Exception ignored) {
      return false;
    }
  }
}
