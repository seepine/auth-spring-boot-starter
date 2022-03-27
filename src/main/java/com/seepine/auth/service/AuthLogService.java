package com.seepine.auth.service;

import com.seepine.auth.entity.LogEvent;
/**
 * 日志接口
 *
 * @author seepine
 */
public interface AuthLogService {
  void save(LogEvent logEvent);
}
