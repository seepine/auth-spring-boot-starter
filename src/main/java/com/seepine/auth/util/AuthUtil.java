package com.seepine.auth.util;

import com.seepine.auth.entity.AuthProperties;
import com.seepine.auth.enums.AuthExceptionType;
import com.seepine.auth.exception.AuthException;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author seepine
 */
@Component
@DependsOn({"redisTemplate", "authProperties"})
public class AuthUtil {
  private static AuthUtil authUtil;
  @Resource private RedisTemplate<String, Object> redisTemplate;
  @Resource private AuthProperties authProperties;
  SnowflakeIdUtil snowflakeIdUtil = new SnowflakeIdUtil(0, 0);

  private final ThreadLocal<Object> THREAD_LOCAL_USER = new ThreadLocal<>();
  private final ThreadLocal<List<String>> THREAD_LOCAL_PERMISSION = new ThreadLocal<>();
  private final ThreadLocal<String> THREAD_LOCAL_TOKEN = new ThreadLocal<>();

  private AuthUtil() {}

  @PostConstruct
  public void init() {
    authUtil = this;
    authUtil.redisTemplate = this.redisTemplate;
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
    return authUtil.authProperties.getCacheKey() + StrUtil.COLON + token + StrUtil.COLON + "user";
  }

  /**
   * 获取权限缓存key
   *
   * @return com.seepine.auth:{token}:permission
   */
  private static String getPermissionKey(String token) {
    return authUtil.authProperties.getCacheKey()
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
      authUtil.redisTemplate.expire(
          getUserKey(authUtil.THREAD_LOCAL_TOKEN.get()), 1, TimeUnit.MILLISECONDS);
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
      authUtil.redisTemplate.expire(
          getPermissionKey(authUtil.THREAD_LOCAL_TOKEN.get()), 1, TimeUnit.MILLISECONDS);
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
    // 设置用户信息过期时间
    authUtil.redisTemplate.expire(
        getUserKey(token), authUtil.authProperties.getTimeout(), authUtil.authProperties.getUnit());
    // 设置用户权限过期时间
    authUtil.redisTemplate.expire(
        getPermissionKey(token),
        authUtil.authProperties.getTimeout(),
        authUtil.authProperties.getUnit());
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
    Object user = authUtil.redisTemplate.opsForHash().get(getUserKey(token), token);
    if (user == null) {
      return false;
    }
    authUtil.THREAD_LOCAL_USER.set(user);

    // 权限相关
    List<String> permission =
        ListUtil.castList(
            authUtil.redisTemplate.opsForHash().get(getPermissionKey(token), token), String.class);
    if (permission != null) {
      authUtil.THREAD_LOCAL_PERMISSION.set(permission);
    }

    // 刷新缓存
    if (authUtil.authProperties.getResetTimeout()) {
      // 刷新用户信息缓存
      authUtil.redisTemplate.expire(
          getUserKey(token),
          authUtil.authProperties.getTimeout(),
          authUtil.authProperties.getUnit());
      // 刷新用户权限缓存
      authUtil.redisTemplate.expire(
          getPermissionKey(token),
          authUtil.authProperties.getTimeout(),
          authUtil.authProperties.getUnit());
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
      authUtil.redisTemplate.expire(
          getUserKey(token),
          authUtil.authProperties.getTimeout(),
          authUtil.authProperties.getUnit());
      // 刷新用户权限缓存
      authUtil.redisTemplate.expire(
          getPermissionKey(token),
          authUtil.authProperties.getTimeout(),
          authUtil.authProperties.getUnit());
    }
  }

  private static void putIntoCache(String token, Object user, List<String> permission) {
    if (StrUtil.isNotBlank(token)) {
      authUtil.THREAD_LOCAL_TOKEN.set(token);
      if (user != null) {
        authUtil.redisTemplate.opsForHash().put(getUserKey(token), token, user);
      }
      if (permission != null) {
        authUtil.redisTemplate.opsForHash().put(getPermissionKey(token), token, permission);
      }
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
      authUtil.redisTemplate.expire(getUserKey(token), 1, TimeUnit.MILLISECONDS);
      authUtil.redisTemplate.expire(getPermissionKey(token), 1, TimeUnit.MILLISECONDS);
    }
  }
}
