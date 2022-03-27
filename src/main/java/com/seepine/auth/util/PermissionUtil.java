package com.seepine.auth.util;

import com.seepine.auth.annotation.Permission;
import com.seepine.auth.annotation.PermissionPrefix;
import com.seepine.auth.enums.AuthExceptionType;
import com.seepine.auth.exception.AuthException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author seepine
 * @since 2.0.0
 */
public class PermissionUtil {
  public static void verify(Permission permission, PermissionPrefix permissionPrefix) {
    List<String> userPermission = AuthUtil.getPermission();

    // 如果类上有前缀注解，并且值不为空串，并且权限注解prefix为true，表示需要拼接前缀
    boolean hasPrefix =
        permissionPrefix != null
            && StrUtil.isNotBlank(permissionPrefix.value())
            && permission.prefix();

    // 1.校验必须包含的权限
    List<String> must = Arrays.asList(permission.value());
    if (hasPrefix) {
      must =
          must.stream().map(item -> permissionPrefix.value() + item).collect(Collectors.toList());
    }
    // 如果权限不为空，并且没有拥有所有需要的权限，阻止
    if (!userPermission.containsAll(must) && !must.isEmpty()) {
      throw new AuthException(AuthExceptionType.NOT_PERMISSION);
    }

    // 2.校验只需满足一个的权限
    List<String> or = Arrays.asList(permission.or());
    if (hasPrefix) {
      or = or.stream().map(item -> permissionPrefix.value() + item).collect(Collectors.toList());
    }
    if (!or.isEmpty()) {
      for (String s : or) {
        // 拥有一个即通过
        if (userPermission.contains(s)) {
          return;
        }
      }
      throw new AuthException(AuthExceptionType.NOT_PERMISSION);
    }
  }
}
