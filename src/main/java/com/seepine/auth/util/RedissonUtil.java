package com.seepine.auth.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author seepine
 * @since 2.0.0 2022.4.9
 */
@Component
public class RedissonUtil {
  private static RedissonUtil redissonUtil;
  private RedissonClient redissonClient;
  private static final long DEFAULT_TIME = 7;
  private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.DAYS;
  private static final String REDIS_LOCK_KEY = "auth_redisson_lock:";

  RedissonUtil(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }

  @PostConstruct
  public void init() {
    redissonUtil = this;
    redissonUtil.redissonClient = this.redissonClient;
  }

  public static RedissonClient getRedissonClient() {
    return redissonUtil.redissonClient;
  }

  /**
   * 设置过期时间
   *
   * @param key key
   * @param duration 期限
   */
  public static void expire(String key, Duration duration) {
    redissonUtil.redissonClient.getBucket(key).expire(duration);
  }

  /**
   * 设置过期时间
   *
   * @param key key
   * @param seconds 秒
   */
  public static void expire(String key, long seconds) {
    expire(key, Duration.ofSeconds(seconds));
  }
  /**
   * 获取缓存
   *
   * @param key key
   * @return value
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(String key) {
    return (T) redissonUtil.redissonClient.getBucket(key).get();
  }

  /**
   * 获取字符串缓存
   *
   * @param key key
   * @return value
   */
  public static String getStr(String key) {
    Object value = get(key);
    return value == null ? null : String.valueOf(value);
  }

  /**
   * 获取整型缓存
   *
   * @param key key
   * @return value
   */
  public static Integer getInt(String key) {
    String value = getStr(key);
    return value == null ? null : Integer.valueOf(value);
  }

  /**
   * 获取长整形缓存
   *
   * @param key key
   * @return value
   */
  public static Long getLong(String key) {
    String value = getStr(key);
    return value == null ? null : Long.valueOf(value);
  }

  /**
   * 移除缓存
   *
   * @param key key
   */
  public static void remove(String key) {
    redissonUtil.redissonClient.getBucket(key).delete();
  }

  /**
   * 判断缓存是否存在
   *
   * @param key key
   * @return boolean
   */
  public static boolean isExists(String key) {
    return redissonUtil.redissonClient.getBucket(key).isExists();
  }

  /**
   * 设置缓存，默认无过期时间
   *
   * @param key key
   * @param value value
   */
  public static void set(String key, Object value) {
    redissonUtil.redissonClient.getBucket(key).set(value);
  }

  /**
   * 设置缓存，使用默认7天过期时间
   *
   * @param key key
   * @param value value
   */
  public static void setWithDefaultExpire(String key, Object value) {
    redissonUtil.redissonClient.getBucket(key).set(value, DEFAULT_TIME, DEFAULT_TIME_UNIT);
  }
  /**
   * 设置缓存
   *
   * @param key key
   * @param value value
   * @param expireTime 过期时间
   * @param timeUnit 时间单位
   */
  public static void set(String key, Object value, long expireTime, TimeUnit timeUnit) {
    redissonUtil.redissonClient.getBucket(key).set(value, expireTime, timeUnit);
  }

  /**
   * 锁定运行
   *
   * @param key 锁值
   * @param apply 执行方法
   */
  public static void sync(Object key, Apply apply) {
    try {
      syncE(key, apply::run);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * 锁定运行，无返回值
   *
   * @param key 锁值
   * @param apply 执行方法
   * @throws Exception 异常
   */
  public static void syncE(Object key, ApplyE apply) throws Exception {
    syncE(
        key,
        () -> {
          apply.runE();
          return true;
        });
  }
  /**
   * 锁定运行，有返回值
   *
   * @param key 锁值
   * @param apply 执行方法
   * @param <T> 返回值类型
   * @return 返回值
   */
  public static <T> T sync(Object key, ApplyAs<T> apply) {
    try {
      return syncE(key, apply::run);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * 锁定运行，有返回值
   *
   * @param key 锁值
   * @param apply 执行方法
   * @param <T> 返回值类型
   * @return 返回值
   * @throws Exception 异常
   */
  public static <T> T syncE(Object key, ApplyAsE<T> apply) throws Exception {
    RLock lock = redissonUtil.redissonClient.getLock(REDIS_LOCK_KEY + key.toString());
    try {
      lock.lock();
      return apply.run();
    } finally {
      lock.unlock();
    }
  }

  public interface Apply {
    void run();
  }

  public interface ApplyE {
    void runE() throws Exception;
  }

  public interface ApplyAs<T> {
    T run();
  }

  public interface ApplyAsE<T> {
    T run() throws Exception;
  }
}
