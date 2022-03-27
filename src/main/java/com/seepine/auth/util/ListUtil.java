package com.seepine.auth.util;

import java.util.ArrayList;
import java.util.List;
/**
 * list工具类
 *
 * @author seepine
 */
public class ListUtil {
  public static <T> List<T> castList(Object obj, Class<T> clazz) {
    List<T> result = new ArrayList<T>();
    if (obj instanceof List<?>) {
      for (Object o : (List<?>) obj) {
        result.add(clazz.cast(o));
      }
      return result;
    }
    return null;
  }
}
