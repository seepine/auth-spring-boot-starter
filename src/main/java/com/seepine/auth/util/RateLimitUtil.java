package com.seepine.auth.util;

import com.seepine.auth.entity.RateLimitEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * @author seepine
 */
@Slf4j
@Component
@DependsOn("redisTemplate")
public class RateLimitUtil {
  @Resource private RedisTemplate<String, Object> redisTemplate;
  private static RateLimitUtil SINGLE;
  private static final String LIMIT_KEY = "com.seepine.rate_limit:";
  private static final Base64.Encoder base64Encoder = Base64.getEncoder();

  private RateLimitUtil() {}

  @PostConstruct
  public void init() {
    SINGLE = this;
    SINGLE.redisTemplate = this.redisTemplate;
  }

  /**
   * 验证是否速率限制
   *
   * @param second 秒速率，如5，则一秒最多只能访问5次
   * @param minute 分速率，同上
   * @param hour 时速率
   * @param day 天速率
   * @param global 是否全局共享
   * @return true/false
   */
  public static boolean verify(int second, int minute, int hour, int day, boolean global) {
    String uniqueId = getUniqueId(global);
    Object cache = SINGLE.redisTemplate.opsForValue().get(LIMIT_KEY + uniqueId);
    RateLimitEntity entity;
    try {
      if (cache == null) {
        throw new Exception();
      }
      entity = (RateLimitEntity) cache;
    } catch (Exception e) {
      entity = new RateLimitEntity();
      entity.init();
      SINGLE.redisTemplate.opsForValue().set(LIMIT_KEY + uniqueId, entity);
      return true;
    }
    LocalDateTime now = LocalDateTime.now();

    // 相差超过1天
    if (beyond(entity.getLastDayTime(), now, TimeUnit.DAYS)) {
      entity.init();
    }
    // 不超过1天
    else {
      // 超过1小时
      if (beyond(entity.getLastHourTime(), now, TimeUnit.HOURS)) {
        entity.setDay(entity.getDay() + 1);
        entity.setHour(1);
        entity.setMinute(1);
        entity.setSecond(1);
        entity.setLastHourTime(now);
        entity.setLastMinuteTime(now);
        entity.setLastSecondTime(now);
      }
      // 不超过1小时
      else {
        // 超过1分钟
        if (beyond(entity.getLastMinuteTime(), now, TimeUnit.MINUTES)) {
          entity.setDay(entity.getDay() + 1);
          entity.setHour(entity.getHour() + 1);
          entity.setMinute(1);
          entity.setSecond(1);
          entity.setLastMinuteTime(now);
          entity.setLastSecondTime(now);
        }
        // 不超过1分钟
        else {
          // 超过1秒钟
          if (beyond(entity.getLastSecondTime(), now, TimeUnit.SECONDS)) {
            entity.setDay(entity.getDay() + 1);
            entity.setHour(entity.getHour() + 1);
            entity.setMinute(entity.getMinute() + 1);
            entity.setSecond(1);
            entity.setLastSecondTime(now);
          }
          // 不超过1秒钟
          else {
            entity.setDay(entity.getDay() + 1);
            entity.setHour(entity.getHour() + 1);
            entity.setMinute(entity.getMinute() + 1);
            entity.setSecond(entity.getSecond() + 1);
          }
        }
      }
    }
    SINGLE.redisTemplate.opsForValue().set(LIMIT_KEY + uniqueId, entity);
    SINGLE.redisTemplate.expire(LIMIT_KEY + uniqueId, 1, TimeUnit.DAYS);
    if (second > 0 && entity.getSecond() > second) {
      log.debug("{}超出秒请求限制,限制数{}，请求数{}", uniqueId, second, entity.getSecond());
      return false;
    }
    if (minute > 0 && entity.getMinute() > minute) {
      log.debug("{}超出分请求限制,限制数{}，请求数{}", uniqueId, minute, entity.getMinute());
      return false;
    }
    if (hour > 0 && entity.getHour() > hour) {
      log.debug("{}超出时请求限制,限制数{}，请求数{}", uniqueId, hour, entity.getHour());
      return false;
    }
    if (day > 0 && entity.getDay() > day) {
      log.debug("{}超出天请求限制,限制数{}，请求数{}", uniqueId, day, entity.getDay());
      return false;
    }
    return true;
  }

  /**
   * 两个时间是否超出时间单位 例如start=2022-1-19 9:39:10:000，end=2022-1-19 9:39:11:000
   *
   * <p>当TimeUnit.MINUTES时，没超出false，因为相差不到1分钟
   *
   * <p>当TimeUnit.SECONDS时，超出true，因为相差超过1秒钟
   *
   * @param start startTime
   * @param end endTime
   * @param unit 当TimeUnit
   * @return boolean
   */
  private static boolean beyond(LocalDateTime start, LocalDateTime end, TimeUnit unit) {
    Duration duration = Duration.between(start, end);
    if (TimeUnit.DAYS.equals(unit)) {
      return duration.toDays() > 0;
    } else if (TimeUnit.HOURS.equals(unit)) {
      return duration.toHours() > 0;
    } else if (TimeUnit.MINUTES.equals(unit)) {
      return duration.toMinutes() > 0;
    } else if (TimeUnit.SECONDS.equals(unit)) {
      return duration.toMillis() > 999;
    }
    // 判断不常用
    else if (TimeUnit.MILLISECONDS.equals(unit)) {
      return duration.toMillis() > 0;
    }
    throw new IllegalArgumentException("not support timeUnit:" + unit.toString());
  }

  /**
   * 获取请求者唯一标识，获取用户id，若id为空则获取ip+ua
   *
   * @param global 是否全局，全局则只取requestURI
   * @return 返回md5
   */
  public static String getUniqueId(boolean global) {
    HttpServletRequest request = HttpServletUtil.getHttpRequest();
    if (request == null) {
      throw new IllegalArgumentException("请求不合法");
    }
    if (StrUtil.isBlank(request.getRequestURI())) {
      throw new IllegalArgumentException("请求不合法");
    }
    if (global) {
      return base64Encoder.encodeToString(request.getRequestURI().getBytes(StandardCharsets.UTF_8));
    }
    String ip = IpUtil.getIp(request);
    String ua = request.getHeader("User-Agent");
    if (StrUtil.isBlank(ip)) {
      ip = request.getSession().getId();
    }
    if (StrUtil.isBlank(ua)) {
      ua =
          "Mozilla/9.9 (Macintosh; Intel Mac OS X 12_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.44";
    }
    return base64Encoder.encodeToString(
        (request.getRequestURI() + StrUtil.AT + ip + StrUtil.AT + ua)
            .getBytes(StandardCharsets.UTF_8));
  }
}
