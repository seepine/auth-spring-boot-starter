package com.seepine.auth.util;

import com.seepine.auth.AutoConfiguration;
import com.seepine.auth.enums.AuthExceptionType;
import com.seepine.auth.exception.AuthException;
import com.seepine.auth.properties.AuthProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author seepine
 */
@Component
@AutoConfigureAfter(AutoConfiguration.class)
public class AuthUtil {
  private static AuthUtil authUtil;
  @Resource private AuthProperties authProperties;
  SnowflakeIdUtil snowflakeIdUtil = new SnowflakeIdUtil(0, 0);

  private final ThreadLocal<Object> THREAD_LOCAL_USER = new ThreadLocal<>();
  private final ThreadLocal<List<String>> THREAD_LOCAL_PERMISSION = new ThreadLocal<>();
  private final ThreadLocal<String> THREAD_LOCAL_TOKEN = new ThreadLocal<>();

  private AuthUtil() {}

  @PostConstruct
  public void init() {
    authUtil = this;
    authUtil.authProperties = this.authProperties;
  }

  public static void clear() {
    authUtil.THREAD_LOCAL_USER.remove();
    authUtil.THREAD_LOCAL_PERMISSION.remove();
    authUtil.THREAD_LOCAL_TOKEN.remove();
  }

  /**
   * 获取用户缓存key
   *
   * @return com.seepine.auth:{token}:user
   */
  private static String getUserKey(String token) {
    return authUtil.authProperties.getCachePrefix()
        + StrUtil.COLON
        + token
        + StrUtil.COLON
        + "user";
  }

  /**
   * 获取权限缓存key
   *
   * @return com.seepine.auth:{token}:permission
   */
  private static String getPermissionKey(String token) {
    return authUtil.authProperties.getCachePrefix()
        + StrUtil.COLON
        + token
        + StrUtil.COLON
        + "permission";
  }

  /**
   * 在controller/service中使用，直接获取当前登录者用户信息
   *
   * @param <T> 范型
   * @return user
   */
  @SuppressWarnings("unchecked")
  public static <T> T getUser() {
    try {
      T user = (T) authUtil.THREAD_LOCAL_USER.get();
      if (user != null) {
        return user;
      }
    } catch (Exception ignored) {
    }
    if (StrUtil.isNotBlank(authUtil.THREAD_LOCAL_TOKEN.get())) {
      RedissonUtil.remove(getUserKey(authUtil.THREAD_LOCAL_TOKEN.get()));
    }
    throw new AuthException(AuthExceptionType.EXPIRED_LOGIN);
  }
  /**
   * 获取用户权限
   *
   * @return permission
   */
  public static List<String> getPermission() {
    try {
      List<String> permission = authUtil.THREAD_LOCAL_PERMISSION.get();
      return permission == null ? new ArrayList<>() : permission;
    } catch (Exception ignored) {
    }
    if (StrUtil.isNotBlank(authUtil.THREAD_LOCAL_TOKEN.get())) {
      RedissonUtil.remove(getPermissionKey(authUtil.THREAD_LOCAL_TOKEN.get()));
    }
    throw new AuthException(AuthExceptionType.EXPIRED_LOGIN);
  }
  /**
   * 登录成功后设置用户信息，并返回token
   *
   * @param user user
   * @return token
   */
  public static String login(Object user) {
    return login(user, null);
  }
  /**
   * 登录成功后设置用户信息，并返回token
   *
   * @param user user
   * @param permission 用户权限
   * @return token
   */
  public static String login(Object user, List<String> permission) {
    String token = authUtil.snowflakeIdUtil.nextIdString();
    putIntoCache(token, user, permission);
    return token;
  }

  /**
   * 通过token获取用户信息
   *
   * @param token token
   * @return bool
   */
  public static boolean findAndFill(String token) {
    // 设置token
    if (StrUtil.isBlank(token)) {
      return false;
    }
    authUtil.THREAD_LOCAL_TOKEN.set(token);

    // 用户相关
    Object user = RedissonUtil.get(getUserKey(token));
    if (user == null) {
      return false;
    }
    authUtil.THREAD_LOCAL_USER.set(user);

    // 权限相关
    List<String> permission =
        ListUtil.castList(RedissonUtil.get(getPermissionKey(token)), String.class);
    if (permission != null) {
      authUtil.THREAD_LOCAL_PERMISSION.set(permission);
    }

    // 刷新缓存
    if (authUtil.authProperties.getResetTimeout()) {
      refresh();
    }
    return true;
  }

  /**
   * 刷新用户信息,当进行了更新等操作 支持该方法为未登录线程赋值user对象，提供getUser()取值用
   *
   * @param user user
   */
  public static void refreshUser(Object user) {
    putIntoCache(authUtil.THREAD_LOCAL_TOKEN.get(), user, null);
  }

  /**
   * 刷新用户权限
   *
   * @param permission 权限列表
   */
  public static void refreshPermission(List<String> permission) {
    putIntoCache(authUtil.THREAD_LOCAL_TOKEN.get(), null, permission);
  }

  /** 主动刷新缓存 */
  public static void refresh() {
    String token = authUtil.THREAD_LOCAL_TOKEN.get();
    if (StrUtil.isNotBlank(token)) {
      // 刷新用户信息缓存
      RedissonUtil.expire(
          getUserKey(token), Duration.ofSeconds(authUtil.authProperties.getTimeout()));
      // 刷新用户权限缓存
      RedissonUtil.expire(
          getPermissionKey(token), Duration.ofSeconds(authUtil.authProperties.getTimeout()));
    }
  }

  /**
   * 供登录或刷新用户及权限方法用
   *
   * @param token token
   * @param user 用户信息
   * @param permission 权限信息
   */
  private static void putIntoCache(String token, Object user, List<String> permission) {
    if (StrUtil.isNotBlank(token)) {
      authUtil.THREAD_LOCAL_TOKEN.set(token);
      if (user != null) {
        RedissonUtil.set(getUserKey(token), user);
      }
      if (permission != null) {
        RedissonUtil.set(getPermissionKey(token), permission);
      }
      refresh();
    }
    // 允许未登录调用此方法设置当前登录者信息及权限，方便后续逻辑获取用户权限
    if (user != null) {
      authUtil.THREAD_LOCAL_USER.set(user);
    }
    if (permission != null) {
      authUtil.THREAD_LOCAL_PERMISSION.set(permission);
    }
  }

  /** 登出 */
  public static void logout() {
    String token = authUtil.THREAD_LOCAL_TOKEN.get();
    if (StrUtil.isNotBlank(token)) {
      RedissonUtil.remove(getUserKey(token));
      RedissonUtil.remove(getPermissionKey(token));
    }
  }
}
