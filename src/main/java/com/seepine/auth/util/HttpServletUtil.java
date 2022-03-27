package com.seepine.auth.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author seepine
 */
public class HttpServletUtil {
  /**
   * 获取request
   *
   * @return HttpServletRequest | null
   */
  public static HttpServletRequest getHttpRequest() {
    if (RequestContextHolder.getRequestAttributes() == null) {
      return null;
    }
    return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
  }
}
