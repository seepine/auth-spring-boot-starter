package com.seepine.auth.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author seepine
 */
@Data
public class RateLimitEntity implements Serializable {
  private static final long serialVersionUID = 1L;
  /** 保存秒执行了几次 */
  int second;
  /** 保存分钟执行了几次 */
  int minute;
  /** 保存小时执行了几次 */
  int hour;
  /** 保存天执行了几次 */
  int day;

  /** 初始化时设为1 */
  public void init() {
    second = 1;
    minute = 1;
    hour = 1;
    day = 1;
    lastSecondTime = lastMinuteTime = lastHourTime = lastDayTime = LocalDateTime.now();
  }
  /** 保存上次执行时间 */
  LocalDateTime lastSecondTime;
  /** 保存上次执行时间 */
  LocalDateTime lastMinuteTime;
  /** 保存上次执行时间 */
  LocalDateTime lastHourTime;
  /** 保存上次执行时间 */
  LocalDateTime lastDayTime;
}
