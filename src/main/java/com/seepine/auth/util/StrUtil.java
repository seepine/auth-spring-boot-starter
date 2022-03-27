package com.seepine.auth.util;

/**
 * @author seepine
 */
public class StrUtil {
  public static final String AT = "@";
  public static final String COLON = ":";
  public static final String LF = "\n";
  public static final String EQUAL = "=";
  public static final String AND = "&";
  public static final String SPACE = " ";
  public static final String EMPTY = "";

  public static boolean isBlank(String str) {
    return str == null || EMPTY.equals(str);
  }

  public static boolean isNotBlank(String str) {
    return !isBlank(str);
  }
}
