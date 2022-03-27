package com.seepine.auth.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取IP方法
 *
 * @author seepine
 */
public class IpUtil {
  /**
   * 获取客户端ip，来源于网络
   *
   * @param request 网络请求
   * @return ip
   */
  public static String getIp(HttpServletRequest request) {
    String ipAddress;
    try {
      if (request == null) {
        return "";
      }
      ipAddress = request.getHeader("x-forwarded-for");
      if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
        ipAddress = request.getHeader("Proxy-Client-IP");
      }
      if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
        ipAddress = request.getHeader("WL-Proxy-Client-IP");
      }
      if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
        ipAddress = request.getRemoteAddr();
        if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
          ipAddress = "127.0.0.1";
        }
      }
      if (ipAddress.length() > 15) { // "***.***.***.***".length()
        // = 15
        if (ipAddress.indexOf(",") > 0) {
          ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
      }
    } catch (Exception e) {
      ipAddress = "";
    }
    return ipAddress;
  }
}
